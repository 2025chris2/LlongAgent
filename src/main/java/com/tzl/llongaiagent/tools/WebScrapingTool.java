package com.tzl.llongaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(
            @ToolParam(description = "URL of the web page to scrape") String url
    ) {
        try {
            Document document = Jsoup.connect(url)
                    .timeout(10000)  // 10秒超时，防止挂死
                    .get();

            String text = document.text();

            // 限制返回长度，防止超长网页撑爆 LLM 上下文
            int maxLength = 8000;
            if (text.length() > maxLength) {
                text = text.substring(0, maxLength) + "\n...[Content truncated]";
            }
            return text;

        } catch (Exception e) {
            return "Error occurred while scraping the web page: " + e.getMessage();
        }
    }
}

