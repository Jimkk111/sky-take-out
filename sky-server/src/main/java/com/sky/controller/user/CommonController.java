package com.sky.controller.user;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

/**
 * 通用接口（用户端）：本地图片下载
 */
@RestController("userCommonController")
@RequestMapping("/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Value("${sky.upload.path:uploads}")
    private String uploadPath;

    /**
     * 本地图片下载（前端通过/common/download?name=xxx访问上传的图片）
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        if (name == null || name.isEmpty()) {
            response.setStatus(404);
            return;
        }
        File file = Paths.get(uploadPath).toAbsolutePath().resolve(name).toFile();
        if (!file.exists() || !file.isFile()) {
            response.setStatus(404);
            return;
        }
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            response.setContentType("image/jpeg");
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            log.error("文件下载失败：{}", e.getMessage());
            response.setStatus(500);
        }
    }
}
