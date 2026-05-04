package com.tzl.llongaiagent.agent;


import cn.hutool.core.util.StrUtil;
import com.tzl.llongaiagent.agent.model.AgentStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/***
 * 抽象的基础代理类，用于管理代理状态和执行流程
 * 提供状态转换，内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现 step 方法
 */
@Slf4j
@Data
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    // 系统提示词
    private String systemPrompt;
    // 引导智能体的下一步操作
    private String nextStepPrompt;

    // 代理状态 (当前Agent的状态)
    private AgentStatus status = AgentStatus.IDLE; // 默认是空闲状态

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 20;

    // LLM大模型
    private ChatClient chatClient;

    // 大模型的上下文 memory记忆 (需要自主维护)
    private List<Message> messageList = new ArrayList<>();


    /***
     * 运行代理
     * @param userPrompt 用户的提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {

        // 1.基础校验
        // 对 agent 状态和提示词合法性 进行判断
        if(this.status != AgentStatus.IDLE) {
            throw new RuntimeException("Cannot run agent form status:" + status);
        }
        if(StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 2. 修改状态(防止运行冲突)
        status = AgentStatus.RUNNING;

        // 3. 记录上下文消息
        messageList.add(new UserMessage(userPrompt));

        // 4. 保存结果列表 (大模型返回的是String,且大模型只认String)
        List<String> results = new ArrayList<>();

        // 这里使用try-catch, 防止报错
        try{
            // 5. 执行循环
            for(int i = 0 ; i < maxSteps && status != AgentStatus.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                // 打印日志
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行结果
                String stepResult = this.step();
                String result = "step " + stepNumber + ":" + stepResult;
                results.add(result);
            }

            // 6. 检查是否超出步骤限制
            if(currentStep == maxSteps){
                this.status = AgentStatus.FINISHED;
                results.add("Terminated: Reached max step (" + maxSteps + ")");
                log.info("Reached maxSteps: " + maxSteps);
            }

            // 7. 将 字符串数组 拼接为 字符串, 用回车分割
            return String.join("\n", results);

        } catch(Exception e){
            status = AgentStatus.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }


    /***
     * 定义单个步骤
     * @return AI模型只认String
     */
    public abstract String step();


    /***
     * 清理资源
     */
    protected void cleanup() {
        // 子类重写清理资源的方法
    }

}

