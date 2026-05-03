package com.tzl.llongaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/***
 * Spring AI 框架调用 AI 大模型
 * 轻量级测试:为了测试该类的方法，实现一个 CommandLineRunner接口，实现一个 run 方法即可
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel chatModel;


    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = chatModel.call(new Prompt("我是NBA的KD，你知道我么？"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}
