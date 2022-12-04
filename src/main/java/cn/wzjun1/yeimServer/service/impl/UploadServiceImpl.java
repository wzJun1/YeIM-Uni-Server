package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.service.UploadService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Override
    public Map<String, Object> getStorageParams() throws Exception {
        Map<String, Object> params = new HashMap<>();
        if ("cos".equals(storageType)) {
            params = getCOSParams();
        } else if ("oss".equals(storageType)) {
            params = getOSSParams();
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
            put("bucket", bucket);
            put("region", region);
            put("policyBase64", policyBase64);
            put("signature", signature);
            put("expireTime", expireTime);
        }};
    }

}




