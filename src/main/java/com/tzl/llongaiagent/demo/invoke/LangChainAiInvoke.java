package com.tzl.llongaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;

public class LangChainAiInvoke {

    public static void main(String[] args){
        ChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String chat = qwenChatModel.chat("你好我是NBA的KD，你认识我么？");
        System.out.println(chat);

    }
}
