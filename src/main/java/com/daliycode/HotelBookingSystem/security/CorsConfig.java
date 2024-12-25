////package com.daliycode.HotelBookingSystem.security;
////
////
////import org.springframework.boot.web.servlet.FilterRegistrationBean;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.http.HttpHeaders;
////import org.springframework.http.HttpMethod;
////import org.springframework.web.cors.CorsConfiguration;
////import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
////import org.springframework.web.filter.CorsFilter;
////import org.springframework.web.servlet.config.annotation.EnableWebMvc;
////
////import java.util.Arrays;
////
////@Configuration
////@EnableWebMvc
////public class CorsConfig {
////
////    private static final Long MAX_AGE = 3600L;
////    private static final Integer CORS_FILTER_ORDER = -102;
////
////    @Bean
////    public FilterRegistrationBean corsFilter() {
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        CorsConfiguration config = new CorsConfiguration();
////        config.setAllowCredentials(true);
////        config.addAllowedOrigin("http://localhost:5173");
////        config.setAllowedHeaders(Arrays.asList(
////                HttpHeaders.AUTHORIZATION,
////                HttpHeaders.CONTENT_TYPE,
////                HttpHeaders.ACCEPT
////        ));
////        config.setAllowedMethods(Arrays.asList(
////                HttpMethod.GET.name(),
////                HttpMethod.POST.name(),
////                HttpMethod.PUT.name(),
////                HttpMethod.DELETE.name()
////        ));
////        config.setMaxAge(MAX_AGE);
////        source.registerCorsConfiguration("/**", config);
////        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
////        bean.setOrder(CORS_FILTER_ORDER);
////        return bean;
////    }
////
////}
//package com.daliycode.HotelBookingSystem.security;
//
//import jakarta.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import java.util.Arrays;
//
//@Configuration
//@EnableTransactionManagement
//@EnableWebMvc
//public class CorsConfig {
//    private static final Long MAX_AGE = 3600L;
//
//    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);
//
//    @PostConstruct
//    public void init() {
//        logger.info("CorsConfig has been loaded");
//    }
//
//    @Bean
//    public FilterRegistrationBean corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOrigin("http://localhost:5173"); // 前端地址
//        config.setAllowedHeaders(Arrays.asList(
//                HttpHeaders.AUTHORIZATION,
//                HttpHeaders.CONTENT_TYPE,
//                HttpHeaders.ACCEPT
//        ));
//        config.setAllowedMethods(Arrays.asList(
//                HttpMethod.GET.name(),
//                HttpMethod.POST.name(),
//                HttpMethod.PUT.name(),
//                HttpMethod.DELETE.name()
//        ));
//        config.setMaxAge(MAX_AGE);
//        source.registerCorsConfiguration("/**", config);
//        return new FilterRegistrationBean(new CorsFilter(source));
//    }
//}
//

package com.daliycode.HotelBookingSystem.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
public class CorsConfig {

    private static final Long MAX_AGE = 3600L; // 缓存时间

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource(); // 变量重命名为 corsSource
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // 设置允许的前端地址
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
        ));
        config.setMaxAge(MAX_AGE);

        corsSource.registerCorsConfiguration("/**", config); // 注册全局 CORS 配置

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsSource)); // 使用重命名后的 corsSource
        bean.setOrder(-102); // 设置优先级
        return bean;
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() { // 新增配置以兼容 Spring Security 的 CORS 配置
        UrlBasedCorsConfigurationSource securityCorsSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration securityConfig = new CorsConfiguration();

        securityConfig.setAllowCredentials(true);
        securityConfig.addAllowedOrigin("http://localhost:5173");
        securityConfig.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        securityConfig.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
        ));
        securityConfig.setMaxAge(MAX_AGE);

        securityCorsSource.registerCorsConfiguration("/**", securityConfig); // 注册 CORS 配置
        return securityCorsSource; // 返回兼容的配置
    }
}
