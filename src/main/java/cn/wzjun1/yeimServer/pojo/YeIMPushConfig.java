package cn.wzjun1.yeimServer.pojo;

import cn.wzjun1.yeimServer.pojo.push.GtConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "yeim.push")
@Getter
@Setter
public class YeIMPushConfig {
    private boolean enable;
    private String type;
    private GtConfig gt;
    private String oppoChannelId;
    private String xiaomiChannelId;
}