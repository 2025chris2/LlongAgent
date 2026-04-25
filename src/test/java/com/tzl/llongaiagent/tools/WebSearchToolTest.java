package com.tzl.llongaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${searchAPi.apiKey}")
    private String api_key;

    @Test
    void searchWeb() {


        WebSearchTool tool = new WebSearchTool(api_key);
        String kevinDruant = tool.searchWeb("Kevin druant");
        Assertions.assertNotNull(kevinDruant);


    }
}