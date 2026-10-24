package com.inkloom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${inkloom.cors.allowed-origins:https://inkloom-ai-xi.vercel.app,http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> originList = new ArrayList<>();
        // Always guarantee the deployed Vercel frontend and local development origins
        originList.add("https://inkloom-ai-xi.vercel.app");
        originList.add("http://localhost:5173");
        originList.add("http://127.0.0.1:5173");
        originList.add("http://localhost:3000");

        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String raw : allowedOrigins.split(",")) {
                String trimmed = raw.trim().replaceAll("/+$", "");
                if (!trimmed.isEmpty() && !originList.contains(trimmed)) {
                    originList.add(trimmed);
                }
            }
        }

        configuration.setAllowedOrigins(originList);
        configuration.setAllowedOriginPatterns(List.of(
                "https://inkloom-ai-xi.vercel.app",
                "https://*-inkloom-ai*.vercel.app",
                "https://*.vercel.app",
                "http://localhost:[*]",
                "http://127.0.0.1:[*]"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-User-Id",
                "Cache-Control"
        ));
        configuration.addAllowedHeader("*");
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
