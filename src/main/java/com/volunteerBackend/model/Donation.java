package com.volunteerBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.volunteerBackend.type.PaymentMethod;
import com.volunteerBackend.type.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "donations")
@Data
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private User donor;

    @Column(name = "donor_name")
    private String donorName;

    @Column(name = "donor_email")
    private String donorEmail;

    @Column(name = "donor_phone")
    private String donorPhone;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * - VNPAY: là vnp_TxnRef
     * - MOMO: là orderId
     */
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    /**
     * - VNPAY: là vnp_TransactionNo
     * - MOMO: là transId
     */
    @Column(name = "transaction_id")
    private String TransactionId;

    @Column(name = "bank_code")
    private String bankCode;
    /**
     * Mã phản hồi từ cổng.
     * - VNPAY: vnp_ResponseCode (00)
     * - MOMO: resultCode (0)
     */
    @Column(name = "response_code")
    private String responseCode;

    /**
     * - VNPAY: lưu vnp_BankTranNo, vnp_CardType, vnp_SecureHash...
     * - MOMO: lưu requestId, payType, extraData...
     */
    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Mã biên lai hoặc URL biên lai
    @Column(name = "receipt_url")
    private String receiptUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}