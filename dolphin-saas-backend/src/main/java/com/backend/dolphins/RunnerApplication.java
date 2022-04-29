package com.backend.dolphins;

import com.alibaba.fastjson.JSON;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@SpringBootApplication
@RestController
@MapperScan(value = "com.dolphin.saas.mapper")
@ComponentScan(basePackages = {"com.dolphin.saas.commons","com.dolphin.saas.service",  "com.backend.dolphins", "com.dolphin.saas.common", "com.backend.dolphins.common"})
public class RunnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunnerApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @RequestMapping("/")
    public Object defaultRoute() {
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("status", 1);
        res.put("data", "dolphin SaaS Backend APi Service V1.0");
        res.put("msg", "启动成功!");
        return JSON.toJSON(res);
    }
}