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

    private static final Long MAX_AGE = 3600L; 

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource(); 
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); 
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

        corsSource.registerCorsConfiguration("/**", config); 

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(corsSource)); 
        bean.setOrder(-102); 
        return bean;
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() { 
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

        securityCorsSource.registerCorsConfiguration("/**", securityConfig); 
        return securityCorsSource; 
    }
}
