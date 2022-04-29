package com.dolphin.openapi.center;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class DealExceptionHandler {
    //指定出现什么异常值执行这个方法
    @ExceptionHandler(Exception.class)
    @ResponseBody//为了返回数据
    public Map<String, Object> defaultExceptionHandler(Exception ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", -1);
        map.put("msg", ex.getMessage());
        return map;
    }
}
