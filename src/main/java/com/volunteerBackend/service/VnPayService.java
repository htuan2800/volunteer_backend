package com.volunteerBackend.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.volunteerBackend.config.VnpayConfig;
import com.volunteerBackend.util.VnpayUtil;

import jakarta.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;

@Service
public class VnPayService {
    private final VnpayConfig vnPayConfig;



    public VnPayService(VnpayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }

    public String createPaymentUrl(HttpServletRequest request, Long donationId, BigDecimal amount, String orderId)
            throws UnsupportedEncodingException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate= dateFormat.format(calendar.getTime());
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang:" + donationId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VnpayUtil.getIpAddress(request));
        vnpParams.put("vnp_CreateDate", createDate);

        calendar.add(Calendar.MINUTE, 15);
        String expirationDate = dateFormat.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", expirationDate);

        List<String> sortedFieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(sortedFieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = sortedFieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Chuỗi dùng để hash **không encode**
                hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        System.out.println("Hash data: " + vnPayConfig.getHashSecret());
        String vnp_SecureHash = VnpayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        System.out.println("Redirect to: " + vnPayConfig.getPayUrl() + "?" + query.toString());
        return vnPayConfig.getPayUrl() + "?" + query.toString();
    }

    public String queryTransaction(String orderId, String transDate, String ipAddress) {
        try {
            String vnp_RequestId = VnpayUtil.getRandomNumber(8);
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_TmnCode = vnPayConfig.getTmnCode();
            String vnp_OrderInfo = "Kiem tra ket qua GD OrderId:" + orderId;

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            // --- Build hash data
            String hashData = String.join("|",
                    vnp_RequestId,
                    vnp_Version,
                    vnp_Command,
                    vnp_TmnCode,
                    orderId,
                    transDate,
                    vnp_CreateDate,
                    ipAddress,
                    vnp_OrderInfo);

            String vnp_SecureHash = VnpayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            // --- Build JSON payload
            JsonObject json = new JsonObject();
            json.addProperty("vnp_RequestId", vnp_RequestId);
            json.addProperty("vnp_Version", vnp_Version);
            json.addProperty("vnp_Command", vnp_Command);
            json.addProperty("vnp_TmnCode", vnp_TmnCode);
            json.addProperty("vnp_TxnRef", orderId);
            json.addProperty("vnp_TransactionDate", transDate);
            json.addProperty("vnp_CreateDate", vnp_CreateDate);
            json.addProperty("vnp_IpAddr", ipAddress);
            json.addProperty("vnp_OrderInfo", vnp_OrderInfo);
            json.addProperty("vnp_SecureHash", vnp_SecureHash);

            // --- Gửi POST request tới VNPay
            URL url = new URI(vnPayConfig.getApiUrl()).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.toString().getBytes());
                os.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
