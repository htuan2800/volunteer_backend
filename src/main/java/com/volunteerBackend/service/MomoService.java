package com.volunteerBackend.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerBackend.config.MomoConfig;
import com.volunteerBackend.util.MomoUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MomoService {
    private final MomoConfig momoConfig;

    public MomoService(MomoConfig momoConfig) {
        this.momoConfig = momoConfig;
    }

    public String createPayment(BigDecimal amount, String orderId) throws Exception {
        String extraData = "";
        String orderInfo = "Thanh toán giao dịch: " + orderId;
        String requestId = UUID.randomUUID().toString();
        Long amountLong = amount.longValue();
        String rawData = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                momoConfig.getAccessKey(), amountLong.toString(), extraData, momoConfig.getIpnUrl(), orderId.toString(),
                orderInfo, momoConfig.getPartnerCode(), momoConfig.getReturnUrl(), requestId,
                momoConfig.getRequestType());
        String signature = MomoUtil.signHmacSHA256(rawData, momoConfig.getSecretKey());
        Map<String, String> requestBody = new HashMap<>();
        // requestBody.put("accessKey", momoConfig.getAccessKey());
        requestBody.put("amount", amountLong.toString());
        requestBody.put("extraData", extraData);
        requestBody.put("ipnUrl", momoConfig.getIpnUrl());
        requestBody.put("orderId", orderId.toString());
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("redirectUrl", momoConfig.getReturnUrl());
        requestBody.put("requestId", requestId);
        requestBody.put("requestType", momoConfig.getRequestType());
        requestBody.put("signature", signature);
        requestBody.put("lang", "vi");

        String response = sendPaymentRequest(momoConfig.getEndPoint(), requestBody);
        Map<String, Object> responseMap = new ObjectMapper().readValue(response, Map.class);
        System.out.println(responseMap);
        return responseMap.get("payUrl").toString();
    }

    private String sendPaymentRequest(String endpoint, Map<String, String> requestBody) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);
        System.out.println(requestBody);
        String json = new ObjectMapper().writeValueAsString(requestBody);
        StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        HttpClientResponseHandler<String> responseHandler = response -> {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                // Nếu thành công, đọc body về chuỗi String
                org.apache.hc.core5.http.HttpEntity responseEntity = response.getEntity();
                return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
            } else {
                // Nếu lỗi (VD: 400 Bad Request của MoMo), vẫn đọc body để xem lỗi gì
                org.apache.hc.core5.http.HttpEntity responseEntity = response.getEntity();
                return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
            }
        };

        // Thực thi và tự động đóng resources
        return client.execute(httpPost, responseHandler);
    }

}
