package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.service.UploadService;
import cn.wzjun1.yeimServer.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Slf4j
@RestController
public class UploadController {


    @Autowired
    UploadService uploadService;

    /**
     * YeIMUniSDK 获取媒体上传参数
     *
     * @description YeIMUniSDK发送媒体消息需要使用到上传功能，此处获取相关上传参数，例如后端设置上传仓库为：腾讯云COS，则在此处获取上传签名等
     */
    @UserAuthorization
    @GetMapping(path = "/upload/sign")
    public Result update() {
        try {
            Object params = uploadService.getStorageParams();
            return Result.success(params);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通用上传
     *
     * @param file
     * @param key
     * @return
     */
    @UserAuthorization
    @PostMapping(path = "/upload")
    public Result<Map<String, Object>> upload(MultipartFile file, @RequestParam @NotNull String key) {
        try {
            return Result.success(uploadService.upload(key, file));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传图片
     *
     * @param file 文件
     * @param key  文件名称，带后缀
     * @return
     */
    @UserAuthorization
    @PostMapping(path = "/upload/image")
    public Result<Map<String, Object>> uploadImage(MultipartFile file, @RequestParam @NotNull String key) {
        try {
            return Result.success(uploadService.uploadImage(key, file));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传视频
     *
     * @param file 文件
     * @param key  文件名称，带后缀
     * @return
     */
    @UserAuthorization
    @PostMapping(path = "/upload/video")
    public Result<Map<String, Object>> uploadVideo(MultipartFile file, @RequestParam @NotNull String key) {
        try {
            return Result.success(uploadService.uploadVideo(key, file));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
