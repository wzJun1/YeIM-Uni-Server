package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.service.UploadService;
import cn.wzjun1.yeimServer.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping (path = "/upload/sign")
    public Result update() {
        try {
            Object params = uploadService.getOSSParams();
            return Result.success(params);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
