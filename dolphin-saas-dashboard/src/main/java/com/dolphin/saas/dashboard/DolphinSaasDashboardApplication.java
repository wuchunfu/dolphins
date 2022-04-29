package com.dolphin.saas.dashboard;

import com.alibaba.fastjson.JSON;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

@SpringBootApplication
@RestController
@MapperScan(value = "com.dolphin.saas.mapper")
@ComponentScan(basePackages = {"com.dolphin.saas.service", "com.dolphin.saas.commons", "com.dolphin.saas.dashboard", "com.dolphin.saas.common", "com.dolphin.saas.dashboard.common"})
public class DolphinSaasDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DolphinSaasDashboardApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @RequestMapping("/")
    public Object defaultRoute() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", 1);
        res.put("data", "dolphin SaaS APi Service V3.0");
        res.put("msg", "启动成功!");
        return JSON.toJSON(res);
    }
}
