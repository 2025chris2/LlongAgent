package com.tzl.llongaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.tzl.llongaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.nio.file.Paths;

/***
 * 资源下载工具
 */
public class ResourceDownloadTool {

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of file to save the download resource") String fileName) {
        String fileDir = Paths.get(FileConstant.FILE_SAVE_DIR, "download").toString();
        String filePath = Paths.get(fileDir, fileName).toString();
        try{
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource download successfully to: " + filePath;
        } catch(Exception e){
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
