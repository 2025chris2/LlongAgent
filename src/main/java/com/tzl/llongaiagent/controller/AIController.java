package com.tzl.llongaiagent.controller;

import com.tzl.llongaiagent.agent.llongManus;
import com.tzl.llongaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private LoveApp loveApp;
    @Autowired
    private llongManus llongManus;

    /***
     * 同步调用 AI 恋爱大师应用
     * @param userMessage
     * @param conversationId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String userMessage, String conversationId) {
        return loveApp.doChat(userMessage, conversationId);
    }

    // 第一种流式推送

    /***
     * SSE 流式调用 AI 恋爱大师应用
     * @param userMessage
     * @param conversationId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String userMessage, String conversationId) {
        return loveApp.doChatByStream(userMessage, conversationId);
    }

    // 第二种流式推送
    @GetMapping("/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String userMessage, String conversationId) {
        return loveApp.doChatByStream(userMessage, conversationId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build()
                );
    }

    // 第三种流式推送
    @GetMapping("/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String userMessage, String conversationId) {
        // 设置三分钟超时
        SseEmitter sseEmitter = new SseEmitter(180000L);

        // 后端向前端推送消息的方式
//        sseEmitter.send("我是llong Agent");

        //获取FLux响应式数据流并且直接通过订阅推送给SseEmitter
        loveApp.doChatByStream(userMessage, conversationId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError,sseEmitter::complete); // 这里lambda简写:当传入的参数是唯一的,且调用的参数也是唯一的,就可以简写了，这里传入与调用的都是e
        return sseEmitter;
    }

    @Resource
    private ChatModel chatModel;

    @Resource
    private ToolCallback[] allTools;

    /***
     * 流式调用 Manus 超级智能体
     *
     * @param userMessage 用户消息
     * @return 流式输出,打字机效果
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String userMessage) {
        llongManus llongManus = new llongManus(allTools, chatModel);
        return llongManus.runStream(userMessage);
    }

}
