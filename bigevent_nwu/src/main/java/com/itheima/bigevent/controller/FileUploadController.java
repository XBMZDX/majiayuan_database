package com.itheima.bigevent.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.utils.AliOssUtil;

import org.springframework.beans.factory.annotation.Value;

@RestController
@CrossOrigin
public class FileUploadController 
{
   // @Value("${upload.path}")
    //private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws Exception
    {
        /*File dir = new File(uploadPath);
        if (!dir.exists()) 
        {
            dir.mkdirs();
        }*/
        //把文件内容存储到本地磁盘上
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + ext;

        String url =  AliOssUtil.uploadFile(newFilename, file.getInputStream());

        return Result.success(url);
    }
}
