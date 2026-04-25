package com.tzl.llongaiagent.app;

import com.tzl.llongaiagent.advisor.MyLoggerAdvisor;
import com.tzl.llongaiagent.advisor.ReReadingAdvisor;
import com.tzl.llongaiagent.chatmemoryreopsitory.FileBasedChatMemoryRepository;
import com.tzl.llongaiagent.rag.LoveAppRAGCustomAdvisor;
import com.tzl.llongaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;


@Component
@Slf4j
public class LoveApp {

    // 聊天客户端
    private final ChatClient chatClient;



    private static final String SYSTEM_PROMPT="扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /***
     * 初始化 AI 客户端,chatClient
     * @param dashscopeModel 灵积的大模型,通过依赖自动注入的
     */
    public LoveApp(ChatModel dashscopeModel){

        // 1.初始化基于内存的对话记忆仓库
//        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

        // 2.初始化基于文件的对话记忆仓库
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        FileBasedChatMemoryRepository fileBasedChatMemoryRepository = new FileBasedChatMemoryRepository(fileDir);

        // 以下部分是通用的，只需改变上面的仓库的初始化，以及选择用哪个仓库即可.
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                // 对于上面的仓库的切换，只需在这里把.chatMemoryRepository里面的参数改变即可
                // 解耦了
                .chatMemoryRepository(fileBasedChatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        // 通过 builder 创建 MessageChatMemoryAdvisor（构造器是 private 的）
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        // 打印日志,官方的日志打印器，但是只打印debug的日志，在yaml中配置logging配置
        SimpleLoggerAdvisor loggerAdvisor = new SimpleLoggerAdvisor();

        // 自定义的日志打印,在包advisor下的MyLoggerAdvisor类
        MyLoggerAdvisor myLoggerAdvisor = new MyLoggerAdvisor();

        // 自定义推理增强 Advisor，可按需开启
        ReReadingAdvisor reReadingAdvisor = new ReReadingAdvisor();

        // 对所有的请求生效
        chatClient = ChatClient.builder(dashscopeModel)
                .defaultSystem(SYSTEM_PROMPT)
                //下面这行代码的顾问是完全的，下下行是比较轻盈的,所以这里用下下行的代码
//              .defaultAdvisors(memoryAdvisor,loggerAdvisor,reReadingAdvisor)
                .defaultAdvisors(memoryAdvisor,myLoggerAdvisor)
                .build();
//        仅对单次请求生效
//        chatClient.prompt()
//                .stream()

    }

    /***
     * AI 基础对话，支持多轮记忆
     * @param userMessage
     * @param chatId
     * @return
     */
    public String doChat(String userMessage, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(userMessage)
                .advisors(
                        // 通过用户ID来区分对话，取当前chatId用户的上下文
                        spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Content:{}",content);
        return content;
    }


    // 添加public属性，让别的包也能访问!
    public record LoveReport(String title, List<String> suggestions){}

    /***
     * AI 恋爱功能报告(实战结构化输出)
     * @param userMessage
     * @param chatId
     * @return AI转换后的LoveReport
     */
    public LoveReport doChatWithReport(String userMessage, String chatId){
        LoveReport entity = chatClient
                .prompt()
                // 添加系统提示词
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .advisors(advisorSpec -> {
                    advisorSpec.param(ChatMemory.CONVERSATION_ID,chatId);// 在花括号里面用";"，上面的方法不在花括号里面
                })
                .call()
                .entity(LoveReport.class);
        log.info("LoveReport: {}", entity);
        return entity;
    }



    // AI 恋爱知识库问答功能

    // 基于内存的知识库
    @Resource
    private VectorStore loveAppVectorStore;

    // 云数据库的向量存储的存储器
//    @Resource
//    private VectorStore pgVectorVectorStore;

    // 基于云知识库
    @Resource
    private Advisor loveAppRAGCloudAdvisor;

    // 注入自定义的重写器
    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRAG(String userMessage, String conversationID) {
        // 查询重写:重写是重写用户的提问，所以这里在用户发送消息前，进行改写
        String rewrittenMessage = queryRewriter.doQueryRewrite(userMessage);

        ChatResponse chatResponse = chatClient.prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> {
                    spec.param(ChatMemory.CONVERSATION_ID, conversationID);
                })
                // 开启日志,自己定义的日志
                .advisors(new MyLoggerAdvisor())

                // 1.基于内存的知识库实现 : 应用 RAG 知识库问答, 核心是加一个知识库问答的顾问!!!
                // 这里的QuestionAnswerAdvisor是用什么知识问答顾问,loveAppVectorStore是从哪个向量数据库中查询
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())

                // 2.使用云知识库实现 :
                // 应用 RAG 检索增强服务(基于云知识库)
//                .advisors(loveAppRAGCloudAdvisor)

                // 3.应用 RAG 检索增强服务(基于 PgVector 向量存储),使用的云数据库(阿里云serverless)
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())

                // 4.应用自定义的 RAG 检索增强服务( 文档查询器 + 上下文增强器)
//                .advisors(LoveAppRAGCustomAdvisor.createLoveAppRAGCustomAdvisor(
//                        loveAppVectorStore,"单身"
//                ))
                .call()
                .chatResponse();
        Assert.notNull(chatResponse, "AI 服务返回异常: chatResponse 为 null");
        Assert.notNull(chatResponse.getResult(), "AI 服务返回异常,result为 null");
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    public String chatWithTools(String userMessage, String conversationId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(userMessage)
                .advisors(spec -> spec.param(
                        ChatMemory.CONVERSATION_ID, conversationId
                ))
                .advisors(new MyLoggerAdvisor())
                // 已经构建好的 ToolCallback 实例,当参数进行传入
                .toolCallbacks(allTools)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

}
