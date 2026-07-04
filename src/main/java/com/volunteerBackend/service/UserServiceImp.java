package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.volunteerBackend.DTO.DashboardOfUserDTO;
import com.volunteerBackend.config.JwtProvider;
import com.volunteerBackend.config.RabbitMQConfig;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.Notification.NotificationType;
import com.volunteerBackend.model.User;
import com.volunteerBackend.model.UserProvider;
import com.volunteerBackend.payload.EmailResetPayload;
import com.volunteerBackend.payload.EmailVerifyPayload;
import com.volunteerBackend.repository.DonationRepository;
import com.volunteerBackend.repository.UserRepository;
import com.volunteerBackend.request.ChangePasswordRequest;
import com.volunteerBackend.request.NotificationRequest;
import com.volunteerBackend.request.RegisterRequest;
import com.volunteerBackend.request.ResetPasswordRequest;
import com.volunteerBackend.request.UserRequest;
import com.volunteerBackend.type.AuthProvider;
import com.volunteerBackend.type.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final CloudinaryStorageService cloudinaryStorageService;
    private final DashboardStatisticsService dashboardStatisticsService;
    private final UserRepository userRepository;

    @Value("${app.verification.token-expiry}")
    private long tokenExpiryMs;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;

    @Override
    public boolean createUser(User user) throws UserException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new UserException("Phone number already exists");
        }
        user.setRole(UserRole.USER);
        user.setEmail(user.getEmail().toLowerCase());
        user.setFullName(user.getFullName().trim());
        user.setPhoneNumber(user.getPhoneNumber().trim());
        user.setGender(user.getGender());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        UserProvider userProvider = new UserProvider();
        userProvider.setProviderName(AuthProvider.LOCAL);
        userProvider.setUser(user);
        user.getProviders().add(userProvider);

        userRepository.save(user);
        dashboardStatisticsService.updateTotalUsers();
        return true;
    }

    @Override
    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        } else {
            return user.get();
        }
    }

    @Override
    public User findUserByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber);
        return user;
    }

    @Override
    public User findUserById(Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserException("User not found");
        }
    }

    @Override
    public User findByFullName(String username) {
        User user = userRepository.findByFullName(username);
        return user;
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public User registerUser(RegisterRequest user) throws Exception {
        // findByEmail trả về Optional<User> nên không cần check != null
        Optional<User> isExist = userRepository.findByEmail(user.getEmail());
        if (isExist.isPresent()) {
            throw new Exception("User already exists");
        }
        User newUser = new User();
        String token = generateVerificationToken();
        newUser.setFullName(user.getFullName());
        newUser.setEmail(user.getEmail());
        newUser.setRole(UserRole.USER);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));

        newUser.setVerificationToken(token);
        newUser.setTokenExpiry(LocalDateTime.now().plus(tokenExpiryMs, ChronoUnit.MILLIS));

        UserProvider userProvider = new UserProvider();
        userProvider.setProviderName(AuthProvider.LOCAL);
        userProvider.setUser(newUser);
        newUser.getProviders().add(userProvider);
        User savedUser = userRepository.save(newUser);
        EmailVerifyPayload payload = new EmailVerifyPayload();
        payload.setToken(token);
        payload.setEmail(user.getEmail());
        payload.setFullname(user.getFullName());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_USER_NAME,
                RabbitMQConfig.ROUTING_REGISTRATION_KEY,
                payload);
        dashboardStatisticsService.updateTotalUsers();
        return savedUser;
    }

    @Override
    public boolean forgotPassword(String email) throws UserException {
        User user = findUserByEmail(email);
        if (user == null) {
            throw new UserException("User not found");
        }
        String token = generateVerificationToken();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plus(tokenExpiryMs, ChronoUnit.MILLIS));
        userRepository.save(user);
        EmailResetPayload payload = new EmailResetPayload();
        payload.setToken(token);
        payload.setEmail(user.getEmail());
        payload.setFullname(user.getFullName());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_USER_NAME,
                RabbitMQConfig.ROUTING_FORGETPASSWORD_KEY,
                payload);
        return true;
    }

    @Override
    public boolean changePassword(ChangePasswordRequest request, User user) throws UserException {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UserException("Mật khẩu cũ không đúng");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewpassword())) {
            throw new UserException("Mật khẩu xác nhận không khớp");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest request) throws UserException {
        Optional<User> userOpt = userRepository.findByVerificationToken(request.getToken());

        if (userOpt.isEmpty()) {
            return false; // Token không tồn tại
        }

        User user = userOpt.get();
        if (!user.getVerificationToken().equals(request.getToken())) {
            throw new UserException("Invalid token");
        }
        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException("Token expired");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        boolean isAlreadyLinked = user.getProviders().stream()
                .anyMatch(p -> p.getProviderName().equals(AuthProvider.LOCAL));
        if(!isAlreadyLinked)
        {
            UserProvider userProvider = new UserProvider();
            userProvider.setProviderName(AuthProvider.LOCAL);
            userProvider.setUser(user);
            user.getProviders().add(userProvider);
        }
        userRepository.save(user);
        return true;
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchUsers(query);
    }

    @Override
    public boolean updateUser(UserRequest user, User existingUser) throws UserException {
        if (existingUser == null) {
            throw new UserException("User not found");
        }
        if (user.getOption().equals("COVER_IMAGE")) {
            if (existingUser.getCoverPhotoURL() != null) {
                cloudinaryStorageService.deleteFile(existingUser.getCoverPhotoURL());
            }
            existingUser.setCoverPhotoURL(user.getCoverPhotoURL());
        } else if (user.getOption().equals("AVATAR_IMAGE")) {
            if (existingUser.getAvatar() != null) {
                if (existingUser.getAvatar().startsWith("http") || existingUser.getAvatar().startsWith("https")) {
                } else {
                    cloudinaryStorageService.deleteFile(existingUser.getAvatar());
                }
            }
            existingUser.setAvatar(user.getAvatar());
        } else {
            existingUser.setFullName(user.getFullName());
            existingUser.setGender(user.getGender());
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        userRepository.save(existingUser);
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(existingUser.getId());
        notificationRequest.setTitle("Thay đổi thông tin cá nhân");
        notificationRequest.setMessage("Thông tin cá nhân của bạn đã được thay đổi vui lòng kiếm tra lại");
        notificationRequest.setRelatedId(null);
        notificationRequest.setType(NotificationType.SYSTEM);
        notificationService.createNotification(notificationRequest);
        return true;
    }

    @Override
    public boolean updateUserByAdmin(UserRequest user, Integer userId) throws UserException {
        Optional<User> user1 = userRepository.findById(userId);
        if (user1.isEmpty()) {
            throw new UserException("User not found");
        }
        User oldUser = user1.get();
        oldUser.setFullName(user.getFullName());
        oldUser.setGender(user.getGender());
        if (user.getOption().equals("IMAGE")) {
            if (StringUtils.hasText(user.getAvatar())) {
                if (StringUtils.hasText(oldUser.getAvatar())) {
                    cloudinaryStorageService.deleteFile(oldUser.getAvatar());
                }
                oldUser.setAvatar(user.getAvatar());
            }
            if (StringUtils.hasText(user.getCoverPhotoURL())) {
                if (StringUtils.hasText(oldUser.getCoverPhotoURL())) {
                    cloudinaryStorageService.deleteFile(oldUser.getCoverPhotoURL());
                }
                oldUser.setCoverPhotoURL(user.getCoverPhotoURL());
            }
        }
        userRepository.save(oldUser);
        System.out.println("DONE");
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(oldUser.getId());
        notificationRequest.setTitle("Thay đổi thông tin cá nhân");
        notificationRequest.setMessage("Thông tin cá nhân của bạn đã được thay đổi vui lòng kiếm tra lại");
        notificationRequest.setRelatedId(null);
        notificationRequest.setType(NotificationType.SYSTEM);
        notificationService.createNotification(notificationRequest);
        return true;
    }

    @Override
    public User findUserByJwt(String Jwt) {
        if (Jwt == null)
            return null;
        Integer id = JwtProvider.getIdFromJwtToken(Jwt);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return null;
        } else {
            return user.get();
        }
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return false; // Token không tồn tại
        }

        User user = userOpt.get();

        // Kiểm tra token đã hết hạn
        if (user.getTokenExpiry().isBefore(LocalDateTime.now()) || user.getIsVerified()) {
            return false; // Token đã hết hạn hoặc email đã được xác nhận trước đó
        }

        // Cập nhật trạng thái verified
        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return true;
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Email đã được xác nhận");
        }

        // Tạo token mới
        String newToken = generateVerificationToken();
        user.setVerificationToken(newToken);
        user.setTokenExpiry(LocalDateTime.now().plus(tokenExpiryMs, ChronoUnit.MILLIS));
        userRepository.save(user);

        // Gửi lại email
        EmailVerifyPayload payload = new EmailVerifyPayload();
        payload.setToken(newToken);
        payload.setEmail(user.getEmail());
        payload.setFullname(user.getFullName());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_USER_NAME,
                RabbitMQConfig.ROUTING_REGISTRATION_KEY,
                payload);
    }

    @Override
    public boolean changeActiveUser(Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserException("User not found");
        }
        user.get().setIsActive(!user.get().getIsActive());
        userRepository.save(user.get());
        return true;
    }

    @Override
    public boolean deleteUser(Integer userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserException("User not found");
        }
        user.get().setIsDeleted(true);
        userRepository.save(user.get());
        return true;
    }

    @Override
    public DashboardOfUserDTO getDashboardOfUser(User user) throws UserException {
        BigDecimal totalDonations = donationRepository.sumAmountDonationOfUser(user.getId());
        Long totalCampaign = donationRepository.countDistinctCampaignsForDonor(user);
        DashboardOfUserDTO dashboardOfUserDTO = new DashboardOfUserDTO();
        dashboardOfUserDTO.setTotalDonations(totalDonations);
        dashboardOfUserDTO.setTotalCampaigns(totalCampaign);
        return dashboardOfUserDTO;
    }
}