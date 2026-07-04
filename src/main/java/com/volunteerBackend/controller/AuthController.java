package com.volunteerBackend.controller;

import java.util.Map;
import org.springframework.http.HttpHeaders;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.config.JwtProvider;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.User;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.request.ChangePasswordRequest;
import com.volunteerBackend.request.ForgorPasswordRequest;
import com.volunteerBackend.request.LoginRequest;
import com.volunteerBackend.request.RegisterRequest;
import com.volunteerBackend.request.ResetPasswordRequest;
import com.volunteerBackend.response.ApiResponse;
import com.volunteerBackend.response.AuthResponse;
import com.volunteerBackend.response.ErrorResponse;
import com.volunteerBackend.response.RegisterResponse;
import com.volunteerBackend.service.UserService;
import com.volunteerBackend.type.UserRole;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    UserService userService;
    
    UserRepository userRepository;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgorPasswordRequest request) throws UserException {
        boolean isSuccess = userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new ApiResponse("Email sent successfully", isSuccess));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String jwt) throws UserException {
        User user = userService.findUserByJwt(jwt);
        boolean isSuccess = userService.changePassword(request, user);
        if (isSuccess) {
            return ResponseEntity.ok(new ApiResponse("Password changed successfully", true));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse("Password change failed", false));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) throws UserException {
        boolean isSuccess = userService.resetPassword(request);
        if (isSuccess) {
            return ResponseEntity.ok(new ApiResponse("Password reset successfully", true));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse("Password reset failed", false));
        }
    }

    @PostMapping("/signup")
    public RegisterResponse createUser(@RequestBody RegisterRequest user) throws Exception {
        User savedUser = userService.registerUser(user);

        RegisterResponse registerResponse = new RegisterResponse("User registered successfully",
                savedUser.getVerificationToken(), true);
        return registerResponse;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean isVerified = userService.verifyEmail(token);

        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse(
                    "Email đã được xác nhận thành công!",
                    true));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(
                            "Token không hợp lệ hoặc đã hết hạn hoặc email đã được xác nhận trước đó.",
                            false));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        System.out.println("Email: " + email);
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok(new ApiResponse("Verification email resent successfully", true));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        User user = userService.findUserByEmail(loginRequest.getEmail());
        if (user == null) {
            throw new BadCredentialsException("User not found");
        } else if ("ADMIN".equals((loginRequest.getRole())) && !user.getRole().equals(UserRole.ADMIN)) {
            throw new BadCredentialsException("Cannot access with this account");
        } else if ("USER".equals((loginRequest.getRole())) && !user.getRole().equals(UserRole.USER)) {
            throw new BadCredentialsException("Cannot access with this account");
        } else if (!user.getIsVerified()) {
            AuthResponse res = new AuthResponse(null, "Login failed", user.getIsVerified());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        } else if(user.getIsDeleted() || !user.getIsActive()) {
            AuthResponse res = new AuthResponse(null, "Login failed", user.getIsVerified());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        } 
        String accessToken = JwtProvider.generateToken(user);
        String refreshToken = JwtProvider.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Không cho JS truy cập
                .secure(false) // Chỉ gửi qua HTTPS
                .path("/") // Cookie có hiệu lực toàn bộ app
                .maxAge(365 * 24 * 60 * 60) // 365 ngày
                .sameSite("Lax") // Ngăn CSRF (có thể dùng "Lax" nếu cần cross-site)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Trả accessToken về cho frontend
        AuthResponse res = new AuthResponse(accessToken, "Login successfully", user.getIsVerified());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) throws UserException {
        // Lấy refresh token từ cookie
        Cookie[] cookies = request.getCookies(); // lấy tất cả cookie gửi kèm request
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                System.out.println("Found refresh token in cookie: " + c.getName());
                if (c.getName().equals("refreshToken")) {
                    refreshToken = c.getValue();
                }
            }
        }

        System.out.println("Refresh token from cookie: " + refreshToken);
        if (refreshToken == null || !JwtProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid or expired refresh token", 401));
        }

        // Giải mã refresh token → tạo access token mới
        Integer userId = JwtProvider.getIdFromJwtToken(refreshToken);
        User user = userService.findUserById(userId);

        String newAccessToken = JwtProvider.refreshAccessToken(user);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, "Token refreshed", user.getIsVerified()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Xóa ngay lập tức
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok("Logged out successfully");
    }
}