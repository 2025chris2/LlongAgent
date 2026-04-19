package com.tzl.llongaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

/***
 * 自定义的DocumentReader,这里为了和官方的加载器进行区分,取名为DocumentLoader
 * 恋爱大师应用文档加载器
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    // 同时处理多个文档!
    private final ResourcePatternResolver resourcePatternResolver;

    // Spring IoC 自动注入参数,此参数是启动类加载器在启动时加载的
    public LoveAppDocumentLoader(ResourcePatternResolver PatternResolver) {
        this.resourcePatternResolver = PatternResolver;
    }

    /***
     * 加载多篇MD文档
     * @return
     */
    public List<Document> loadMarkDown() {
        // 新建一个ArrayList<Document>()存储加载出来的md文档
        List<Document> allDocuments = new ArrayList<>();

        try{
            // 多个文件,resources用来存储从resourcePatternResolver解析出来的文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            // 遍历多个文件,对单个文件进行操作
            for(Resource resource : resources){
                // 获取文件名称
                String fileName = resource.getFilename();
                assert fileName != null;

                // MD文档的阅读器的配置设置
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        // 遇到水平分割线（---）就分割成新文档
                        .withHorizontalRuleCreateDocument(true)
                        // 不读取代码块内容
                        .withIncludeCodeBlock(false)
                        // 不读取引用块（> 开头）
                        .withIncludeBlockquote(false)
                        // 附加元数据：文件名 ,作用： 给解析后的文档打上标签，记录这个内容来自哪个文件。
                        .withAdditionalMetadata("filename", fileName)
                        .build();

                // 创建MarkDown阅读器,传入资源与配置
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                // markdownDocumentReader.get()方法是获取到已经分好的List<Document>,将返回值添加到所有的文档列表中addAll()
                allDocuments.addAll(markdownDocumentReader.get());
            }
        }catch(IOException e){
            log.error("MarkDown 文档加载失败!",e);
        }
        return allDocuments;

    }
}
