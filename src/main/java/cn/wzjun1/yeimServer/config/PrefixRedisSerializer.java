package cn.wzjun1.yeimServer.config;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Order
@Configuration
public class PrefixRedisSerializer extends StringRedisSerializer {

    @Value("${yeim.redis.prefix}")
    private String prefix;

    /**
     * 序列化
     *
     * @param s key
     * @return 结果
     */
    @Override
    public byte[] serialize(String s) {
        if (s == null) {
            return new byte[0];
        }
        String realKey = prefix + s;
        return super.serialize(realKey);
    }

    /**
     * 反序列化
     *
     * @param bytes 数据
     * @return 结果
     */
    @Override
    public String deserialize(byte[] bytes) {
        String s = bytes == null ? null : new String(bytes);
        if (StringUtils.isBlank(s)) {
            return s;
        }
        int index = s.indexOf(prefix);
        if (index != -1) {
            return s.substring(index + 2);
        }
        return s;
    }
}