package com.volunteerBackend.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.volunteerBackend.Handler.OAuth2LoginSuccessHandler;
import com.volunteerBackend.service.CustomOAuth2UserService;
import com.volunteerBackend.service.CustomOidcUserService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Configuration // Đánh dấu lớp này là một lớp cấu hình của Spring.
@EnableWebSecurity // Kích hoạt Spring Security cho ứng dụng.
public class AppConfig {
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @PostConstruct
    public void init() {
        System.out.println("AppConfig initialized");
        System.out.println("OAuth2LoginSuccessHandler: " + oAuth2LoginSuccessHandler);
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(
                management -> management.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS)) // Spring Security sẽ không tạo hoặc lưu trữ session.
                .authorizeHttpRequests(Authorize -> Authorize
                        .requestMatchers("/api/auth/reset-password-phone", "/api/auth/verify-token").permitAll()
                        .requestMatchers("/api/**").authenticated() // Yêu cầu các API có đường dẫn bắt đầu bằng /api phải đăng nhập mới được truy cập.
                        .anyRequest().permitAll()) // Cho phép tất cả các request khác (không phải /api/**) được truy cập mà không cần đăng nhập.
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(customOidcUserService) // << Xử lý thông tin user sau khi đăng nhập thành công
                        .userService(customOAuth2UserService) // << Xử lý thông tin user sau khi đăng nhập thành công
                    )
                    .successHandler(oAuth2LoginSuccessHandler ) // << Xử lý sau khi đăng nhập thành công hoàn toàn
                )
                .addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // .httpBasic(withDefaults())
        // .formLogin(withDefaults());
        // Tắt bảo vệ CSRF vì ứng dụng REST API thường sử dụng JWT, không cần cơ chế bảo
        // vệ CSRF.
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedOrigins(Arrays.asList(
                        "http://localhost:5175",
                        "https://49j386n7-5175.asse.devtunnels.ms"
                ));
                cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Liệt kê cụ thể các
                                                                                                 // phương thức
                cfg.setAllowCredentials(true);
                cfg.setAllowedHeaders(Collections.singletonList("*"));
                cfg.setExposedHeaders(Arrays.asList("Authorization"));
                cfg.setMaxAge(3600L);
                return cfg;
            }
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
