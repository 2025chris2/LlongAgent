package com.tzl.llongimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool tool;

    @Test
    void searchImage() {
        String s = tool.searchImage("basketball");
        Assertions.assertNotNull(s);
    }
}