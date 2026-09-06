package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private AliOssProperties aliOssProperties;

    @Value("${sky.upload.path:uploads}")
    private String uploadPath;

    /**
     * 文件上传
     * OSS配置有效时上传至阿里云OSS；否则保存到本地磁盘并通过下载接口访问
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file, HttpServletRequest request) {
        log.info("文件上传：{}", file);

        try {
            //获取原始文件名的扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            //构造新文件名称，避免文件名重复
            String objectName = UUID.randomUUID() + extension;

            String filePath;
            if (isOssConfigured()) {
                //上传文件到阿里云OSS
                filePath = aliOssUtil.upload(file.getBytes(), objectName);
            } else {
                //OSS未配置，保存到本地磁盘
                Path dir = Paths.get(uploadPath).toAbsolutePath();
                Files.createDirectories(dir);
                file.transferTo(new File(dir.toFile(), objectName));
                //返回下载接口的绝对路径，供浏览器直接访问
                filePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                        + "/admin/common/download/" + objectName;
                log.info("文件已保存到本地：{}", filePath);
            }
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    /**
     * 判断OSS是否已配置有效凭证
     */
    private boolean isOssConfigured() {
        String accessKeyId = aliOssProperties.getAccessKeyId();
        return accessKeyId != null && !accessKeyId.trim().isEmpty() && !accessKeyId.contains("请填写");
    }
}
