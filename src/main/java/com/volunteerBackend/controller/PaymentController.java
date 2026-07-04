package com.volunteerBackend.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.request.MoMoIPN;
import com.volunteerBackend.response.InfoResponse;
import com.volunteerBackend.response.VnPayIpnResponse;
import com.volunteerBackend.service.DonateService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    
    private final DonateService donateService;

    // @PutMapping("/payments/update_payment/{id}")
    // public ResponseEntity<?> updatePayment (@PathVariable String id, @RequestBody
    // PaymentRequest paymentRequest)
    // {
    // boolean isSuccess= donateService.updateDonate(id, paymentRequest);
    // InfoResponse<String> response = new InfoResponse<>(isSuccess, "Payment
    // updated", id);
    // return new ResponseEntity<>(response, HttpStatus.OK);
    // }

    @GetMapping("/api/payments/verify_payment/{id}")
    public ResponseEntity<?> CheckVerifyPayment (@PathVariable String id)
        {
        boolean isSuccess= donateService.verifyUpdate(id);
        InfoResponse<String> response = new InfoResponse<>(isSuccess, "Payment Success", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/momo/ipn-handler")
    public ResponseEntity<?> handleMoMoIPN(@RequestBody MoMoIPN ipnData) throws Exception {

        try {
            // Gọi Service xử lý toàn bộ logic
            donateService.updateDonateMomo(ipnData);

            // Trả về 204 No Content (Theo tài liệu MoMo)
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            // Lỗi chữ ký: Có thể trả về 400 để MoMo biết request sai
            System.err.println("MoMo Security Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // Lỗi logic khác: Vẫn nên trả về 204 để MoMo không retry (spam) IPN nữa
            // Hoặc trả về 500 nếu muốn MoMo thử lại sau
            System.err.println("MoMo Error: " + e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/IPN")
    public ResponseEntity<?> vnpayIpn(HttpServletRequest request) {
        System.out.println("--- VNPAY IPN HANDLER START ---");
        
        try {
            // 1. In ra toàn bộ tham số VNPAY gửi lên để kiểm tra
            Map<String, String> params = new HashMap<>();
            for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
                String name = names.nextElement();
                String value = request.getParameter(name);
                params.put(name, value);
                // Log từng tham số để soi xem có thiếu gì không
                System.out.println("PARAM: " + name + " = " + value);
            }
            // 2. Gọi Service
            System.out.println("Đang gọi Service updateDonateVnPay...");
            VnPayIpnResponse response = donateService.updateDonateVnPay(params);
            
            // 3. Log kết quả Service trả về
            System.out.println("Service trả về: " + response.getRspCode() + " - " + response.getMessage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // --- ĐÂY LÀ PHẦN QUAN TRỌNG NHẤT ---
            System.err.println("!!! LỖI XẢY RA TRONG IPN !!!");
            
            // In toàn bộ lỗi chi tiết ra màn hình (Stack Trace)
            e.printStackTrace(); 
            
            // Trả về mã lỗi 99 (Lỗi không xác định) để VNPAY biết
            VnPayIpnResponse errorResponse = new VnPayIpnResponse("99", "Unknown Error: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
}
