package com.backend.dolphins;

//@Configuration
//public class MyWebMvcConfigurer implements WebMvcConfigurer {
//    @Bean
//    public JWTInterceptor getJWTInterceptor() {
//        return new JWTInterceptor();
//    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(getJWTInterceptor())
//                .addPathPatterns("/**") // 其他非登录接口都需要进行token验证
//                .excludePathPatterns("/")
//                .excludePathPatterns("/webjars/**")
//                .excludePathPatterns("/login/register")
//                .excludePathPatterns("/callback/index")
//                .excludePathPatterns("/login/in");  //登录接口不用于token验证
//    }
//}
