package com.tzl.llongaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/***
// * 恋爱大师向量数据库配置(初始化基于内存的向量数据库(SimpleVectorStore类) Bean)
// */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    // MarkDown的Reader,自定义的加载器
    private LoveAppDocumentLoader documentLoader;

    @Resource
    private MyTextSplitter myTextSplitter;

    @Resource
    private MyKeyWordEnricher enricher;

    @Bean
    public VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        // 构造SpringAI 内置的基于内存的向量数据库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 通过自定义的 documentLoader 加载业务 Markdown 文档
        List<Document> documents = documentLoader.loadMarkDown();

        // 使用自定义的文档切分器，自主切分文档
        List<Document> splitDocuments = myTextSplitter.splitDocuments(documents);

        // 自动补充关键词元信息
        List<Document> enrichDocuments = enricher.enrichDocuments(documents);

        // 把加载的文档存入向量数据库中
        simpleVectorStore.add(documents);

        // 把该向量数据库返回,里面是有数据的
        return simpleVectorStore;

    }
}
