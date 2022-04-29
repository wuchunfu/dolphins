package com.dolphin.saas.jobs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableRetry
@SpringBootApplication
@EnableScheduling
@MapperScan(value = "com.dolphin.saas.mapper")
@ComponentScan(basePackages = {"com.dolphin.saas.service", "com.dolphin.saas.commons", "com.dolphin.saas.jobs", "com.dolphin.saas.common"})
public class DolphinSaasTimedtaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(DolphinSaasTimedtaskApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
