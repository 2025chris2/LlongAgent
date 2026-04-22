package com.tzl.llongaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 基于 AI 的文档元信息增强器(通过 AI 为文档补充元信息)
 */
@Component
public class MyKeyWordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        // 第一个参数是给一个ChatModel，第二个元信息的数量,
        // 通过LLM给文档进行一个元信息分类
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        // 返回增强后的文档
        return enricher.apply(documents);
    }
}
