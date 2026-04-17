package com.tzl.llongaiagent.app;

import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class LoveApp {

    private ChatClient chatClient;



    private static final String SYSTEM_PROMPT="扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /***
     * 初始化 AI 客户端,chatClient
     * @param dashscopeModel 灵积的大模型,通过依赖自动注入的
     */
    public LoveApp(ChatModel dashscopeModel){
        // 初始化基于内存的对话记忆
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        // 通过 builder 创建 MessageChatMemoryAdvisor（构造器是 private 的）
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        // 对所有的请求生效
        chatClient = ChatClient.builder(dashscopeModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(memoryAdvisor)
                .build();
//        仅对单次请求生效
//        chatClient.prompt()
//                .stream()

    }

    /***
     * AI 基础对话，支持多轮记忆
     * @param userMessage
     * @param chatId
     * @return
     */
    public String doChat(String userMessage, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(userMessage)
                .advisors(
                        // 通过用户ID来区分对话，取当前chatId用户的上下文
                        spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Content:{}",content);
        return content;
    }
}
