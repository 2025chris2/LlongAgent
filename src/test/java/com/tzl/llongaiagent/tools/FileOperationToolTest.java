package com.tzl.llongaiagent.tools;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "tzl.txt";
        String result = tool.readFile(fileName);
        Assert.notNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String filePath = "TTT.txt";
        String content = "你好，我是KD!";
        String result = tool.writeFile(filePath, content);
        Assert.notNull(result);
    }
}