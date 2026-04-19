package com.tzl.llongaiagent.rag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
// 开启事务 → 测试完成后自动回滚 → 不污染数据库
    // 如果你想要数据库有数据,就把@Transactional关闭了
//@Transactional
class PgVectorVectorStoreConfigTest {

    @Autowired
    private VectorStore pgVectorVectorStore;



    @Test
    void pgVectorVectorStore() {
        // 1. 构造测试文档
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2"))
        );

        // 2. 存入向量库
        pgVectorVectorStore.add(documents);

        // 3. 相似度搜索
        List<Document> results = pgVectorVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("Spring")
                        .topK(5)
                        .build()
        );

        // 4. 强化断言（真正验证功能）
        Assert.notNull(results, "搜索结果不能为空");
        // 应该至少返回 1 条结果
        assertTrue(results.size() > 0, "未搜索到相关文档");
        // 第一条结果应该包含 Spring 关键字
        assertTrue(results.get(0).getText().contains("Spring AI"), "结果未匹配到 Spring 相关内容");
    }
}
