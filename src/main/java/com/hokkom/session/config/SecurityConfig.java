package com.hokkom.session.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // REST API는 보통 CSRF 비활성화

        http.formLogin(AbstractHttpConfigurer::disable); // 폼 로그인 안 씀

        http.httpBasic(AbstractHttpConfigurer::disable); // Basic Auth 안 씀

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/signup", "/auth/login", "/auth/logout").permitAll()
                .anyRequest().authenticated()
        );

        http.sessionManagement(session -> session.maximumSessions(1));

        return http.build();
    }
}
