package com.backend.dolphins.controller;

import com.dolphin.saas.commons.clouds.comFinal.MasterCommon;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
    public Map<String, Object> handleError(HttpServletRequest request) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return new MasterCommon().JsonResponseStr(-1, df.format(new Date()), "服务响应失败，请稍后再试!");
    }
}
