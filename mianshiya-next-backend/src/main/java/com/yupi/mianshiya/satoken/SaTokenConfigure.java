package com.yupi.mianshiya.satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 全局拦截器（为了支持权限注解）
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    // 注册 Sa-Token 拦截器，打开注解式鉴权功能 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

}