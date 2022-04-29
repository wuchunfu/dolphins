package com.dolphin.saas.commons;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class JWTInterceptor implements HandlerInterceptor {
    @Autowired
    RedisCommonUtils redisCommonUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //获取请求头中的token令牌
        String token = request.getHeader("token");
        try {
            if (token != null) {
                if (redisCommonUtils.hasKeys(token)) {
                    JwtUtil.verify(token);
                    return true;
                }
            }
        } catch (SignatureVerificationException e) {
            map.put("msg", "无效签名!");
        } catch (TokenExpiredException e) {
            map.put("msg", "token过期!");
        } catch (AlgorithmMismatchException e) {
            map.put("msg", "token算法不一致!");
        } catch (Exception e) {
            map.put("msg", "token无效!");
        } finally {
            map.put("msg", "token不存在!");
        }
        map.put("code", -99);//设置状态
        String json = new ObjectMapper().writeValueAsString(map);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json);
        return false;
    }
}
