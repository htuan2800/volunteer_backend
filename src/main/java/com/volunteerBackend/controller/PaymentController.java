package com.volunteerBackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.request.PaymentRequest;
import com.volunteerBackend.service.DonateService;

@RestController
public class PaymentController {
    @Autowired
    private DonateService donateService;

    @PutMapping("/payments/update_payment/{id}")
    public ResponseEntity<?> updatePayment (@PathVariable String id, @RequestBody PaymentRequest paymentRequest)
    {
        String isSuccess= donateService.updateDonate(id, paymentRequest);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
}
