package com.volunteerBackend.service;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.volunteerBackend.model.User;
import com.volunteerBackend.model.UserProvider;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.type.AuthProvider;
import com.volunteerBackend.type.UserRole;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        System.out.println("CustomOidcUserService initialized");
    }

    @Transactional
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("DEBUG: Entering loadUser (OIDC) with provider: " + userRequest.getClientRegistration().getRegistrationId());

        OidcUser oidcUser = super.loadUser(userRequest);
        String providerNameStr = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider providerEnum = AuthProvider.valueOf(providerNameStr.toUpperCase());
        // Provider (VD: google)
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        // Lấy các trường từ ID Token (OIDC)
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();
        String providerId = oidcUser.getSubject(); // sub = ID định danh duy nhất

        System.out.println("DEBUG: OIDC Email = " + email);
        System.out.println("DEBUG: OIDC Claims = " + oidcUser.getClaims());

        if (email == null || email.isEmpty()) {
            System.out.println("Email not found from OIDC provider");
            throw new OAuth2AuthenticationException("Email not found from OIDC provider");
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
        System.out.println("User saved or updated successfully: " + user.getEmail());

        // Trả về DefaultOidcUser để Spring Security set Authentication
        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}
