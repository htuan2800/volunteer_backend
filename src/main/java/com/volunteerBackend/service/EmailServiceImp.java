package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.volunteerBackend.payload.EmailResetPayload;
import com.volunteerBackend.payload.EmailVerifyPayload;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailServiceImp implements EmailService {

    
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async("emailTaskExecutor")
    @Override
    public CompletableFuture<Void> sendVerificationEmailWithAsync(EmailVerifyPayload payload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(payload.getEmail());
            helper.setSubject("Xác nhận tài khoản của bạn");
            
            String verificationLink = baseUrl + "/verify-email?token=" + payload.getToken();
            String htmlContent = buildVerificationEmailTemplate(payload.getFullname(), verificationLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);

            return CompletableFuture.completedFuture(null);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Xác nhận tài khoản của bạn");
            
            String verificationLink = baseUrl + "/verify-email?token=" + token;
            String htmlContent = buildVerificationEmailTemplate(fullName, verificationLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailTemplate(String fullName, String verificationLink) {
         return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f4f4f4; padding: 20px; text-align: center;">
                    <h1 style="color: #333;">Xác nhận tài khoản</h1>
                </div>
                <div style="padding: 20px;">
                    <h2>Xin chào %s,</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng click vào nút bên dưới để xác nhận email của bạn:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Xác nhận Email
                        </a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="word-break: break-all; color: #666;">%s</p>
                    <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 24 giờ.</p>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationLink, verificationLink);
    }

    @Override
    public void sendDonationThankYouEmail(String to, String fullName, BigDecimal amount, String projectName, String transactionCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Cảm ơn bạn đã ủng hộ dự án " + projectName);
            
            String htmlContent = buildDonationThankYouEmailTemplate(fullName, amount, projectName, transactionCode);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Ném ra một exception runtime để consumer có thể bắt được
            throw new RuntimeException("Failed to send thank you email", e);
        }
    }

    private String buildDonationThankYouEmailTemplate(String fullName, BigDecimal amount, String projectName, String transactionCode) {
        // Định dạng số tiền cho đẹp hơn
        DecimalFormat formatter = new DecimalFormat("###,###,### VNĐ");
        String formattedAmount = formatter.format(amount);

        // Nếu fullName rỗng hoặc null (trường hợp ủng hộ ẩn danh), thay bằng một cụm từ chung
        String recipientName = (fullName == null || fullName.trim().isEmpty()) ? "nhà hảo tâm" : fullName;

        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd;">
                    <div style="background-color: #007bff; color: white; padding: 20px; text-align: center;">
                        <h1 style="margin: 0;">Ghi nhận đóng góp</h1>
                    </div>
                    <div style="padding: 30px 20px;">
                        <h2>Xin chào %s,</h2>
                        <p>Thay mặt đội ngũ dự án, chúng tôi xin gửi lời cảm ơn chân thành nhất đến bạn vì đã đóng góp cho dự án <strong>%s</strong>.</p>
                        <p>Sự ủng hộ của bạn là nguồn động viên to lớn giúp chúng tôi tiếp tục sứ mệnh của mình.</p>
                        <div style="background-color: #f9f9f9; padding: 20px; margin: 20px 0; border-left: 5px solid #007bff;">
                            <h3 style="margin-top: 0;">Chi tiết giao dịch:</h3>
                            <p><strong>Số tiền:</strong> <span style="color: #28a745; font-weight: bold;">%s</span></p>
                            <p><strong>Dự án ủng hộ:</strong> %s</p>
                            <p><strong>Mã giao dịch:</strong> %s</p>
                        </div>
                        <p>Chúng tôi sẽ liên tục cập nhật tiến độ của dự án. Một lần nữa, xin chân thành cảm ơn!</p>
                        <p>Trân trọng,<br>Đội ngũ dự án.</p>
                    </div>
                </body>
                </html>
                """.formatted(recipientName, projectName, formattedAmount, projectName, transactionCode);
    }

    @Override
    public void sendForgotPasswordEmail(EmailResetPayload payload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(payload.getEmail());
            helper.setSubject("Reset Password");
            
            String resetPasswordLink = baseUrl + "/reset-password?token=" + payload.getToken();
            String htmlContent = buildForgotPasswordEmailTemplate(payload.getFullname(), resetPasswordLink);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildForgotPasswordEmailTemplate(String fullName, String resetPasswordLink) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f4f4f4; padding: 20px; text-align: center;">
                    <h1 style="color: #333;">Yêu cầu đặt lại tài khoản</h1>
                </div>
                <div style="padding: 20px;">
                    <h2>Xin chào %s,</h2>
                    <p>Bạn vừa có yêu cầu đặt lại tài khoản. Vui lòng click vào nút bên dưới để đổi mật khẩu của bạn:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Đặt lại mật khẩu
                        </a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="word-break: break-all; color: #666;">%s</p>
                    <p><strong>Lưu ý:</strong> Link này sẽ hết hạn sau 1 giờ.</p>
                </div>
            </body>
            </html>
            """.formatted(fullName, resetPasswordLink, resetPasswordLink);
    }
        

}
