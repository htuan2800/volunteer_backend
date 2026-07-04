package com.volunteerBackend.config;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import lombok.RequiredArgsConstructor;

@Configuration // Đánh dấu lớp này là một lớp cấu hình của Spring.
@EnableWebSecurity // Kích hoạt Spring Security cho ứng dụng.
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOidcUserService customOidcUserService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // <-- Dùng EntryPoint của bạn
                )

                .authorizeHttpRequests(Authorize -> Authorize
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // only ADMIN
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/users/profile").authenticated()
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(customOidcUserService)
                        .userService(customOAuth2UserService)
                    )
                    .successHandler(oAuth2LoginSuccessHandler ) // << Xử lý sau khi đăng nhập thành công
                )
                .addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // .httpBasic(withDefaults())
        // .formLogin(withDefaults());
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowedOrigins(Arrays.asList(
                        "http://localhost:5175"
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
