package com.tzl.llongaiagent.chatmemoryreopsitory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileBasedChatMemoryRepository implements ChatMemoryRepository {

    private final String baseDir;

    // Kryo 线程本地实例（非线程安全，每个线程独立）
    private static final ThreadLocal<Kryo> KRYO = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    /**
     * 构造方法 - 从配置文件读取目录，或使用默认路径
     */
    public FileBasedChatMemoryRepository(
            @Value("${spring.ai.chat.memory.file.dir:${user.dir}/tmp/chat-memory}") String dir
    ) {

        this.baseDir = dir;
        File directory = new File(dir);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("创建会话存储目录失败: {}", dir);
                throw new IllegalStateException("无法创建会话存储目录: " + dir);
            }
        }

        log.info("文件会话存储目录: {}", dir);
    }

    /***
     * 这段代码的作用是：扫描指定目录，找出所有 .kryo 格式的对话数据文件，提取文件名（去掉后缀）作为对话ID列表返回。
     * 例如，如果目录中有：
     * conv001.kryo
     * conv002.kryo
     * data.txt（被过滤掉）
     * 返回的列表就是：["conv001", "conv002"]
     * @return  返回的列表就是：["conv001", "conv002"]
     */
    @Override
    public List<String> findConversationIds() {
    // 创建File对象指向基础目录（注意：baseDir为null时会抛异常,这里的baseDir是由我们给予的,所以不用担心）
    File dir = new File(baseDir);

    // 获取目录下所有以.kryo结尾的文件，返回null表示目录不存在或无法访问
    File[] files = dir.listFiles((d, name) -> name.endsWith(".kryo"));

    // 防御性编程：目录不存在/不可读时返回空列表
    if (files == null) {
        return new ArrayList<>();
    }

    // 使用Stream流处理：提取文件名并去掉.kryo后缀，得到对话ID列表
    return Arrays.stream(files)
            .map(f -> f.getName().replace(".kryo", ""))
            .collect(Collectors.toList());
}

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 根据对话ID获取对应的存储文件对象（包含文件路径信息）
        File file = getConversationFile(conversationId);

        // 文件不存在，返回空列表
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // FileInputStream是读取硬盘中的数据,input是将FileInputStream读取的数据喂给KRYO
        try (Input input = new Input(new FileInputStream(file))) {
            // 抑制编译器警告注解。
            // 原因：下一行代码 readObject(input, ArrayList.class) 返回的是原始类型 ArrayList，但你要赋值给泛型类型 List<Message>
            // 编译器担心类型不安全（比如文件里存的其实不是 Message 对象，而是其他类型），会报 "unchecked cast"（未经检查的类型转换）警告。
            // 加上这个注解就是告诉编译器："我知道有风险，不用警告我。
            @SuppressWarnings("unchecked")
            // 核心反序列化逻辑
            // KRYO.get()：获取 Kryo 序列化器的实例（可能是 ThreadLocal 存储的单例）。
            // readObject(input, ArrayList.class)：从 input 流中读取二进制数据，还原成 ArrayList 对象。
            // 由于 Kryo 序列化时不保存泛型信息（擦除），所以只能指定 ArrayList.class，返回的列表理论上应该只包含 Message 对象（取决于写入时的情况）。
                    // 从 input 流中反序列化出 ArrayList 对象。
            List<Message> messages = KRYO.get().readObject(input, ArrayList.class);
            return messages != null ? messages : new ArrayList<>();
        } catch (FileNotFoundException e) {
            log.warn("会话文件不存在: {}", conversationId);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("读取会话数据失败: {}", conversationId, e);
            // 数据损坏时删除文件，避免重复报错
            file.delete();
            return new ArrayList<>();
        }
    }

    /***
     * 覆盖内容保存
     * @param conversationId
     * @param messages
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // getConversationFile是获取对话文件路径,File file指向这个路径就相当于是获取对话文件了
        File file = getConversationFile(conversationId);

        // FileOutputStream 是 JDK 的文件字节输出流，负责向硬盘写入数据；Output 是 Kryo 的包装类，为 Kryo 序列器提供高效的二进制输出能力。
        try (Output output = new Output(new FileOutputStream(file))) {
            // KRYO.get() 获取 Kryo 序列化器实例；writeObject 将 messages 列表序列化为二进制数据，通过 Output 写入文件。
            KRYO.get().writeObject(output, messages);
            log.debug("保存会话成功: {}, 消息数: {}", conversationId, messages.size());
        } catch (IOException e) {
            log.error("保存会话失败: {}", conversationId, e);
            throw new RuntimeException("保存会话失败: " + conversationId, e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("删除会话文件失败: {}", conversationId);
            } else {
                log.debug("删除会话成功: {}", conversationId);
            }
        }
    }

    /**
     * 获取会话文件路径
     * 对 conversationId 进行安全处理，避免路径遍历攻击
     */
    private File getConversationFile(String conversationId) {
        // 替换非法字符，防止目录遍历
        String safeId = conversationId.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        return new File(baseDir, safeId + ".kryo");
    }
}

