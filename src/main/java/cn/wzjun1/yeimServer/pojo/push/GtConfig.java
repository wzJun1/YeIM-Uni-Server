package cn.wzjun1.yeimServer.pojo.push;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
public class GtConfig {
    private String appId;
    private String appKey;
    private String masterSecret;
}