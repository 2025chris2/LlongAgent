package com.tzl.llongaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * Search API 的调用，来实现WebSearch
 */
public class WebSearchTool {

    // Search API 的搜索接口地址
    private final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String API_KEY;

    public WebSearchTool(String apiKey){
        this.API_KEY = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query Keyword") String query
    ){

        // 这里的HashMap是存放后续要发送的 SearchAPI 的所有查询参数
        Map<String,Object> paramMap = new HashMap<>();

        // 查询参数
        paramMap.put("q", query);

        // apikey
        paramMap.put("api_key", API_KEY);

        // 查询的引擎
        paramMap.put("engine", "baidu");

        try{

            // 发送 HTTP 请求：调用 Hutool 的 HttpUtil.get() 方法，向 SEARCH_API_URL 发送 GET 请求，
            // paramMap 会被自动拼接成 URL 查询参数（如 ?q=xxx&api_key=xxx&engine=baidu）。返回的响应体（JSON 字符串）存入 response。
            // 注意：SEARCH_API_URL 末尾有一个空格，可能导致请求 404
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);

            // 取出返回结果的前五条，解析 JSON：使用 Hutool 的 JSONUtil.parseObj() 将服务器返回的 JSON 字符串解析成 JSONObject 对象，方便后续按字段提取数据。
            // JSONObject 类本质是一个键值对映射类,通过访问 键 来获取 值
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 提取 organic_results 部分,提取搜索结果数组：从 JSON 对象中获取键为 "organic_results" 的字段，这是一个 JSON 数组，包含了百度返回的自然搜索结果（标题、链接、摘要等）。
            // 注意：如果 API 返回错误或没有这个字段，organicResults 会是 null。
            // 这里的Array数组里面，每一个元素都是JSONObject对象
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            // 截取前 5 条：调用 subList(0, 5) 截取数组的前 5 个元素。严重问题：如果搜索结果不足 5 条（比如只有 2 条），这里会抛出 IndexOutOfBoundsException，直接跳转到 catch 块返回错误
            // 这里用Object类，因为不确定数组里面的对象
            List<Object> objects = organicResults.subList(0, 5);

            // 拼接搜索结果为字符串,Stream 处理：将截取到的 5 条结果转为 Java Stream，准备逐个映射转换。obj 是 JSONArray 中的单个元素，类型为 Object。
            String result = objects.stream().map(obj -> {

                // 类型强转：将 Stream 中的每个 Object 强转为 JSONObject。
                // 这里假设数组中的每个元素都是 JSON 对象（符合 Search API 的返回格式）。
                JSONObject tmpJSONObject = (JSONObject) obj;

                // 转为字符串：调用 JSONObject.toString()，将该条搜索结果（包含 title、link、snippet 等字段）序列化为 JSON 字符串。
                // 问题：直接 toString() 会输出转义后的紧凑 JSON（如 {"title":"xxx","link":"yyy"}），多条用逗号拼接后变成一大坨转义字符，AI 模型读起来很吃力。
                return tmpJSONObject.toString();

                // 收集拼接：将 Stream 中处理后的 5 个字符串用逗号 "," 连接成一个完整的 String，赋值给 result。
            }).collect(Collectors.joining(","));

            // 返回成功结果：将拼接好的 5 条搜索结果字符串返回给调用方（通常是 AI 模型）。
            return result;

        } catch(Exception e){

            // 返回错误信息：将异常信息拼接成字符串返回。问题：这样返回后，AI 模型会收到一个以 "Error searching Baidu" 开头的字符串，可能会误以为是正常搜索结果的一部分，而不是一个真正的错误。
            // 建议改用日志记录 + 抛出自定义异常，或返回更明确的错误标识。
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}
