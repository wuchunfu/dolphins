package com.dolphin.saas.jobs.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApiIgnore
@Controller
public class dolphinErrorController extends AbstractErrorController {

    public dolphinErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    /**
     * 默认错误返回页面控制
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object handleError(HttpServletRequest request) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> res = new HashMap<>();
        res.put("status", 1);
        res.put("data", df.format(new Date()));
        res.put("msg", "dolphin Timedtask");
        return JSON.toJSON(res);
    }
}
