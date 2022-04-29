package com.dolphin.openapi.center;

import com.dolphin.saas.commons.JWTInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public JWTInterceptor getJWTInterceptor() {
        return new JWTInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(getJWTInterceptor())
//                .addPathPatterns("/**") // 其他非登录接口都需要进行token验证
//                .excludePathPatterns("/")
//                .excludePathPatterns("/swagger-resources/**")
//                .excludePathPatterns("/swagger-ui.html/**")
//                .excludePathPatterns("/webjars/**")
//                .excludePathPatterns("/clue/reservationInfo") // 线索接口
//                .excludePathPatterns("/login/register")
//                .excludePathPatterns("/login/codes")
//                .excludePathPatterns("/login/in");  //登录接口不用于token验证
    }
}