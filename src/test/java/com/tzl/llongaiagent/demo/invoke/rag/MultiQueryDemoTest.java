package com.tzl.llongaiagent.demo.invoke.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiQueryDemoTest {

    @Resource
    private MultiQueryDemo multiQueryDemo;

    @Test
    void expand() {
        List<Query> list = multiQueryDemo.expand("谁是库里？");
        Assertions.assertNotNull(list);
    }
}