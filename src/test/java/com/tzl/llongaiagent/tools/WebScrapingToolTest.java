package com.tzl.llongaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String s = tool.scrapeWebPage("https://www.codefather.cn");
        System.out.println(s);
        Assertions.assertNotNull(s);
    }

}