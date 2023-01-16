package cn.wzjun1.yeimServer.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UploadService {
    Map<String, Object> getStorageParams() throws Exception;

    Map<String, Object> upload(String filename, MultipartFile file) throws Exception;

    Map<String, Object> uploadImage(String filename, MultipartFile file) throws Exception;

    Map<String, Object> uploadVideo(String filename, MultipartFile file) throws Exception;


}
