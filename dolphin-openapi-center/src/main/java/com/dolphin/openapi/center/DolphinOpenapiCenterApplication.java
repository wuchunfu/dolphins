package com.dolphin.openapi.center;

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
@ComponentScan(basePackages = {"com.dolphin.saas.service", "com.dolphin.saas.commons", "com.dolphin.openapi.center", "com.dolphin.saas.common"})
public class DolphinOpenapiCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(DolphinOpenapiCenterApplication.class, args);
	}

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
	}

	@RequestMapping("/")
	public Object defaultRoute() {
		Map<String, Object> res = new HashMap<>();
		res.put("status", 1);
		res.put("data", "dolphin OpenAPi Service V1.0");
		res.put("msg", "启动成功!");
		return JSON.toJSON(res);
	}
}