package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.service.UploadService;
import cn.wzjun1.yeimServer.utils.JavaCvUtil;
import cn.wzjun1.yeimServer.utils.MD5Util;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

/**
 * @author wzjun1
 */
@Service
@Slf4j
public class UploadServiceImpl implements UploadService {


    @Value("${yeim.file.storage.type}")
    String storageType;

    @Value("${yeim.file.storage.customDomain}")
    String customDomain;

    @Value("${yeim.file.storage.bucket}")
    String bucket;

    @Value("${yeim.file.storage.region}")
    String region;

    @Value("${yeim.file.storage.secretId}")
    String secretId;

    @Value("${yeim.file.storage.secretKey}")
    String secretKey;

    @Value("${yeim.file.storage.baseDir}")
    String baseDir;

    @Override
    public Map<String, Object> getStorageParams() throws Exception {
        Map<String, Object> params = new HashMap<>();
        if ("cos".equals(storageType)) {
            params = getCOSParams();
        } else if ("oss".equals(storageType)) {
            params = getOSSParams();
        } else if ("local".equals(storageType)) {
            params = getLocalParams();
        } else {
            throw new Exception("未选择上传仓库");
        }
        return params;
    }

    /**
     * 获取腾讯云对象存储上传临时签名
     *
     * @return
     */
    private Map<String, Object> getCOSParams() {
        long nowTime = System.currentTimeMillis() / 1000 - 1;
        long expireTime = nowTime + 86400; //签名过期时间为当前 + 86400s
        String qKeyTime = nowTime + ";" + expireTime;
        String signKey = HmacUtils.hmacSha1Hex(secretKey, qKeyTime);
        return new HashMap<String, Object>() {{
            put("storage", "cos");
            put("customDomain", customDomain);
            put("baseDir", baseDir);
            put("bucket", bucket);
            put("region", region);
            put("nowTime", nowTime);
            put("expireTime", expireTime);
            put("secretId", secretId);
            put("signKey", signKey);
        }};
    }

    /**
     * 获取阿里云对象存储上传临时签名
     *
     * @return
     */
    private Map<String, Object> getOSSParams() {

        long nowTime = System.currentTimeMillis() / 1000 - 1;
        long expireTime = nowTime + 86400; //签名过期时间为当前 + 86400s
        Instant instant = Instant.ofEpochMilli(expireTime * 1000 + 111);
        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("expiration", instant);//设置该Policy的失效时间，超过这个失效时间之后，就没有办法通过这个policy上传文件了
        policyMap.put("conditions", Collections.singletonList(Arrays.asList("content-length-range", 0, 1048576000)));// 设置上传文件的大小限制
        System.out.println(JSONObject.toJSONString(policyMap));
        String policyBase64 = Base64.encodeBase64String(JSONObject.toJSONString(policyMap).getBytes());
        byte[] bytes = HmacUtils.hmacSha1(secretKey, policyBase64);
        String signature = Base64.encodeBase64String(bytes);
        return new HashMap<String, Object>() {{
            put("storage", "oss");
            put("accessKeyId", secretId);
            put("customDomain", customDomain);
            put("baseDir", baseDir);
            put("bucket", bucket);
            put("region", region);
            put("policyBase64", policyBase64);
            put("signature", signature);
            put("expireTime", expireTime);
        }};
    }

    /**
     * 获取本地上传参数
     *
     * @return
     */
    private Map<String, Object> getLocalParams() {
        return new HashMap<String, Object>() {{
            put("storage", "local");
            put("customDomain", customDomain);
        }};
    }

    /**
     * 通用上传接口
     *
     * @param filename
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> upload(String filename, MultipartFile file) throws Exception {
        //文件类型检验
        String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        // 判断文件后缀
        if (!extName.matches("(aac|mp3|mp4|png|jpg|jpeg|webp|bmp|tiff|rar|zip|7z|xls|xlsx|doc|docx|ppt)")) {
            // "文件格式错误"
            throw new Exception("当前类型文件不允许上传");
        }
        //创建保存目录
        mkFileDirs(filename);

        File desc = new File(baseDir + File.separator + filename);
        file.transferTo(desc);
        return new HashMap<String, Object>() {{
            put("url", "/" + filename);
        }};
    }

    /**
     * 上传图片
     *
     * @param filename
     * @param file
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadImage(String filename, MultipartFile file) throws Exception {

        //文件类型检验
        String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        // 判断是不是图片文件后缀
        if (!extName.matches("(png|jpg|jpeg|webp|bmp|tiff)")) {
            // "文件格式错误"
            throw new Exception("当前类型文件不允许上传");
        }

        //图片保存绝对路径
        String fileAbsoluteDir = mkFileDirs(filename);
        //图片保存相对路径
        String fileRelativeDir = filename.substring(0, filename.lastIndexOf("/"));

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        File desc = new File(baseDir + File.separator + filename);
        //图片压缩0.5
        Thumbnails.of(originalImage)
                .scale(1f)
                .outputQuality(0.5f)
                .toFile(desc.getAbsolutePath());

        //读取图片宽高
        BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(desc.toPath()));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        //创建缩略图
        double thumbnailHeight = 0;
        double thumbnailWidth = 0;
        if (height > 198) {
            double d = 198.0 / height;
            thumbnailHeight = height * d;
            thumbnailWidth = width * d;
        } else if (width > 198) {
            double d = 198.0 / width;
            thumbnailHeight = height * d;
            thumbnailWidth = width * d;
        } else {
            thumbnailHeight = height;
            thumbnailWidth = width;
        }
        //缩略图宽高
        int finalThumbnailWidth = (int) thumbnailWidth;
        int finalThumbnailHeight = (int) thumbnailHeight;

        //缩略图文件相对路径
        String thumbFileName = fileRelativeDir + "_thumb." + extName;

        //保存缩略图
        File thumb = new File(baseDir + File.separator + thumbFileName);
        Thumbnails.of(desc.getAbsolutePath())
                .size(finalThumbnailWidth, finalThumbnailHeight)
                .outputQuality(0.8f)
                .toFile(thumb.getAbsolutePath());

        //可访问图片相对路径URL
        String imageRelativeUrl = "/" + filename;

        //可访问缩略图相对路径URL
        String thumbnailRelativeUrl = "/" + thumbFileName;

        return new HashMap<String, Object>() {{
            put("url", imageRelativeUrl);
            put("thumbnailUrl", thumbnailRelativeUrl);
            put("thumbnailWidth", finalThumbnailWidth);
            put("thumbnailHeight", finalThumbnailHeight);
        }};
    }

    /**
     * 上传视频
     *
     * @param filename
     * @param file
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadVideo(String filename, MultipartFile file) throws Exception {

        //文件类型检验
        String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        // 判断是不是图片文件后缀
        if (!extName.matches("(mp4|mov|avi|flv|3gp|rmvb)")) {
            //文件格式错误
            throw new Exception("当前类型文件不允许上传");
        }

        //视频保存绝对路径
        String fileAbsoluteDir = mkFileDirs(filename);
        //视频保存相对路径
        String fileRelativeDir = filename.substring(0, filename.lastIndexOf("/"));

        //随机文件名
        String videoName = MD5Util.encode(filename + System.currentTimeMillis() + LoginUserContext.getUser().getUserId()) + "." + extName;

        //保存视频文件
        File desc = new File(fileAbsoluteDir + File.separator + videoName);
        file.transferTo(desc);

        //可访问视频文件相对路径
        String videoRelativeUrl = "/" + fileRelativeDir + "/" + videoName;

        //创建视频封面
        String thumbnail = JavaCvUtil.getVideoCover(desc.getAbsolutePath(), fileAbsoluteDir);
        //可访问视频封面图片相对路径
        String thumbnailUrl = "/" + fileRelativeDir + "/" + thumbnail;

        return new HashMap<String, Object>() {{
            put("url", videoRelativeUrl);
            put("thumbnailUrl", thumbnailUrl);
        }};
    }


    /**
     * 根据文件名称路径创建目录
     *
     * @param filename 文件名称
     * @return path
     */
    private String mkFileDirs(String filename) throws Exception {
        String[] path = filename.split("/");
        if (path.length > 0) {
            StringBuilder dirStr = new StringBuilder();
            for (int i = 0; i < path.length; i++) {
                if (i != path.length - 1) {
                    dirStr.append(path[i] + File.separator);
                }
            }
            File dir = new File(baseDir + File.separator + dirStr);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        }
        throw new Exception("创建目录失败");
    }
}




