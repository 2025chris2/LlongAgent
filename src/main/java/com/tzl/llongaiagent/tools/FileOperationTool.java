package com.tzl.llongaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.tzl.llongaiagent.constant.FileConstant;
import okio.Path;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/***
 * 文件操作工具类(提供文件读写功能)
 */
public class FileOperationTool {

    // 定义一个文件保存的路径
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    // 这里的读写操作返回的都是String类型,是因为还要把结果返回给AI,如果是别的类型,AI可能还要转换,犯错
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read")String fileName) {

        // 判断用户给入的文件名称是否合法
        if(isValidFileName(fileName)) return "Invalid file name";

        // 拼写的路径
        String filePath = Paths.get(FILE_DIR, fileName).toString();

        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e){
            return "Error reading file" + e.getMessage();
        }
    }

    @Tool(description = "write content to a file")
    public String writeFile(@ToolParam(description = "Name of file to write")String fileName, @ToolParam(description = "Content to write to the file")String content) {

        // 是否合法
        if (!isValidFileName(fileName)) return "Invalid file name";

        // 拼接
        String filePath = Paths.get(FILE_DIR, fileName).toString();

        try {
            // 创建目录,如果目录存在就不创建,如果不存在就创建一个新的目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (IORuntimeException e) {
            return "Error writing to file: " + e.getMessage();
        }
    }

    private Boolean isValidFileName(String fileName) {
        return fileName != null && !fileName.isEmpty()
                && !fileName.contains("..")
                && !fileName.contains("/")
                && !fileName.contains("\\");
    }

}
