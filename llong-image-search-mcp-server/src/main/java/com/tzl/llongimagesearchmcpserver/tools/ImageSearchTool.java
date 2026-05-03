package com.tzl.llongimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageSearchTool {

    @Value("${pexels.api-key}")  // 从配置注入
    private String apiKey;

    private static final String API_URL = "https://api.pexels.com/v1/search";

    @McpTool(description = "search image from web")
    public String searchImage(@McpToolParam(description = "search query keyword") String query) {
        try {
            List<String> images = searchMediumImage(query);
            return images.isEmpty() ? "No images found." : String.join(",", images);
        } catch (Exception e) {
            log.error("Image search failed for query: {}", query, e);
            return "Image search failed.";
        }
    }

    private List<String> searchMediumImage(String query) {
        Map<String, String> headers = Map.of("Authorization", apiKey);

        HttpResponse httpResponse = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form("query",query)   // GET 请求会自动转换为 query string
                .execute();

        if (httpResponse.getStatus() != 200) {
            throw new RuntimeException("Pexels API returned status: " + httpResponse.getStatus());
        }

        JSONObject json = JSONUtil.parseObj(httpResponse.body());
        JSONArray photos = json.getJSONArray("photos");
        if (photos == null || photos.isEmpty()) {
            return Collections.emptyList();
        }

        return photos.stream()
                .map(JSONUtil::parseObj)
                .map(photo -> photo.getByPath("src.medium", String.class))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
