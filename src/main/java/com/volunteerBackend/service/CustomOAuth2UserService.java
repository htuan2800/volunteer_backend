package com.volunteerBackend.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.User;
import com.volunteerBackend.model.UserProvider;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.type.AuthProvider;
import com.volunteerBackend.type.UserRole;

import jakarta.transaction.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    public CustomOAuth2UserService() {
        System.out.println("CustomOAuth2UserService initialized");
    }

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println(
                "DEBUG: Entering loadUser with provider: " + userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("DEBUG: OAuth2User attributes: " + oAuth2User.getAttributes());

        // Lấy provider name (google, facebook)
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        AuthProvider providerEnum = AuthProvider.valueOf(providerName.toUpperCase());

        // ← QUAN TRỌNG: Xử lý khác nhau giữa các provider
        String email = extractEmail(oAuth2User, providerName);
        String name = extractName(oAuth2User, providerName);
        String picture = extractPicture(oAuth2User, providerName);
        String providerId = oAuth2User.getName();

        // Kiểm tra email có tồn tại không
        if (email == null || email.isEmpty()) {
            System.out.println("Email not found from OAuth2 provider");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            System.out.println("Existing user found: " + email);
            user = userOptional.get();

            boolean isAlreadyLinked = user.getProviders().stream()
                    .anyMatch(p -> p.getProviderName().equals(providerEnum));
            if (!isAlreadyLinked) {
                UserProvider userProvider = new UserProvider();
                userProvider.setProviderName(providerEnum);
                userProvider.setProviderId(providerId);
                userProvider.setUser(user);
                user.getProviders().add(userProvider);
            } else {
                System.out.println("User already linked with provider: " + providerName);
            }

            // Cập nhật thông tin user
            // user.setFullName(name);
            // user.setAvatar(picture);
        } else {
            System.out.println("Creating new OIDC user...");
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setAvatar(picture);
            user.setIsVerified(true);
            user.setRole(UserRole.USER);
            user.setPassword(null);

            UserProvider userProvider = new UserProvider();
            userProvider.setProviderName(providerEnum);
            userProvider.setProviderId(providerId);
            userProvider.setUser(user);
            user.getProviders().add(userProvider);
        }
        userRepository.save(user);
        System.out.println("User saved with email: " + user.getEmail());
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                "email");
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        return oAuth2User.getAttribute("email");
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return oAuth2User.getAttribute("name");
            case "facebook":
                return oAuth2User.getAttribute("name");
            default:
                return oAuth2User.getAttribute("name");
        }
    }

    private String extractPicture(OAuth2User oAuth2User, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                // Google trả về: "picture": "https://..."
                return oAuth2User.getAttribute("picture");

            case "facebook":
                // Facebook trả về nested object: "picture": { "data": { "url": "https://..." }
                Map<String, Object> pictureObj = oAuth2User.getAttribute("picture");
                if (pictureObj != null && pictureObj.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) pictureObj.get("data");
                    return (String) data.get("url");
                }
                // fallback nếu không có trong attributes
                String id = oAuth2User.getAttribute("id");
                return "https://graph.facebook.com/" + id + "/picture?type=large";

            default:
                return null;
        }
    }
}