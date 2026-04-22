package com.tzl.llongaiagent.demo.invoke.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiQueryDemo {

    @Resource
    private ChatClient.Builder chatClient;

    public List<Query> expand(String query) {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClient)
                .numberOfQueries(3)
                .build();
        return queryExpander.expand(new Query(query));
    }
}
