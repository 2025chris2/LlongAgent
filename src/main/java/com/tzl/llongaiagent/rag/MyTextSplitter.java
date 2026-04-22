package com.tzl.llongaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 自定义基于 Token 的切词器
 */
@Component
public class MyTextSplitter {

    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        return textSplitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter textSplitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
        return textSplitter.apply(documents);
    }

}
