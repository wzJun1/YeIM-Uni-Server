package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String, Object> getOSSParams() throws Exception {
        Map<String, Object> params = new HashMap<>();
        if ("cos".equals(storageType)) {
            params = getCosParams();
        } else {
            throw new Exception("未选择上传仓库");
        }
        return params;
    }

    /**
     * 腾讯云对象存储临时signKey
     * @return
     */
    private Map<String, Object> getCosParams() {
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
}




