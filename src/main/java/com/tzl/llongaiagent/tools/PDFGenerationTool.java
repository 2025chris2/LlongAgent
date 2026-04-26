package com.tzl.llongaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.tzl.llongaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * PDF 生成工具
 * 该类封装了使用 iText 7 库生成 PDF 文件的功能
 */
public class PDFGenerationTool {

    /**
     * 生成 PDF 文件
     * @Tool 注解标记该方法为可调用工具（如 Spring AI 框架），returnDirect=false 表示返回值需经过框架处理
     */
    @Tool(description = "Generate a PDF file with given content", returnDirect = false)
    public String generatePDF(
            // @ToolParam 注解描述参数含义，供 AI 调用时理解参数用途
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        // 拼接 PDF 保存目录：基于系统常量目录 + /pdf 子目录
        String fileDir = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf").toString();
        // 拼接完整文件路径：目录路径 + 分隔符 + 文件名
        String filePath = Paths.get(fileDir, fileName).toString();

        try {
            // 使用 FileUtil（如 Hutool）创建目录，若目录不存在则自动创建（支持多级目录）
            FileUtil.mkdir(fileDir);

            // try-with-resources 自动管理资源关闭：
            // 1. PdfWriter: 负责将 PDF 内容写入磁盘文件
            // 2. PdfDocument: 代表 PDF 文档的底层对象
            // 3. Document: iText 7 的高层 API，提供便捷的文档操作方法
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
                //         .toAbsolutePath().toString();
                // PdfFont font = PdfFontFactory.createFont(fontPath,
                //         PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

                // 使用内置字体（当前使用）】
                // 使用 iText 7 内置的 Adobe 中文字体 STSongStd-Light
                // "UniGB-UCS2-H" 是编码标识，表示使用 Unicode 的 GB 编码子集，支持中文显示
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");

                // 为整个文档设置默认字体，后续添加的所有文本都将使用此字体渲染
                document.setFont(font);

                // 创建 Paragraph（段落）对象，将传入的内容包装成 PDF 段落
                Paragraph paragraph = new Paragraph(content);

                // 将段落添加到文档中，此时内容被写入 PDF 页面
                document.add(paragraph);

            } // 自动关闭 document → pdf → writer，释放资源并刷新缓冲区到磁盘

            // 返回成功信息，包含生成的文件完整路径
            return "PDF generated successfully to: " + filePath;

        } catch (IOException e) {
            // 捕获 IO 异常（如目录创建失败、文件写入失败、字体加载失败等）
            // 返回错误提示信息，包含异常的具体消息
            return "Error generating PDF: " + e.getMessage();
        }
    }
}

