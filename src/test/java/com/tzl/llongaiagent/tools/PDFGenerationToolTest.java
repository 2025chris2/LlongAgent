package com.tzl.llongaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName="编程导航原创项目.pdf";
        String content="编程导航原创项目https://www.codefather.cn";
        String result = tool.generatePDF(fileName, content);
        Assertions.assertNotNull(result);
    }
}