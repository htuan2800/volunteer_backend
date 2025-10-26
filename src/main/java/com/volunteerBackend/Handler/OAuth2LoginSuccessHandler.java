package com.volunteerBackend.Handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.volunteerBackend.config.JwtProvider;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

     @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        System.out.println("Email in OAuth2LoginSuccessHandler: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

        // Tạo Access Token
        String accessToken = JwtProvider.generateToken(authentication, user);

        // Tạo Refresh Token
        String refreshToken = JwtProvider.generateRefreshToken(authentication, user);

        // Set refreshToken vào HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // true nếu dùng HTTPS
                .path("/")
                .maxAge(365 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Chuyển hướng về frontend với access token
        String redirectUrl = "http://localhost:5175/oauth2/redirect?token=" + accessToken;
        // String redirectUrl = "https://49j386n7-5175.asse.devtunnels.ms/oauth2/redirect?token=" + accessToken;
        response.sendRedirect(redirectUrl);
    }
}