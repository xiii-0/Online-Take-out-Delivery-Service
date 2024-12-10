package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil util;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件类型后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 利用UUID构造新文件名 （避免同名文件存储失败的问题）
        String objName = UUID.randomUUID().toString() + extension;

        try {
            // 通过OSS上传文件并返回云端存储路径
            String path = util.upload(file.getBytes(), objName);
            if (path != null)
                return Result.success(path);
        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }

        log.info("文件上传失败，OSS服务异常");
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
