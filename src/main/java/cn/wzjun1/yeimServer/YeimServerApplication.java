package cn.wzjun1.yeimServer;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("cn.wzjun1.yeimServer.mapper")
@EnableTransactionManagement
@SpringBootApplication
@EnableAsync
@Slf4j
public class YeimServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YeimServerApplication.class, args); 
    }

}
