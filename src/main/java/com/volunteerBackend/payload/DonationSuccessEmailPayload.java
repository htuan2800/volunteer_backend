package com.volunteerBackend.payload;

import java.io.Serializable;
import java.math.BigDecimal;

// Implement Serializable để đảm bảo có thể truyền qua message queue an toàn
public class DonationSuccessEmailPayload implements Serializable {
    private String to;
    private String fullName;
    private BigDecimal amount;
    private String projectName;
    private String transactionCode;

    // Cần có constructor rỗng để Jackson có thể deserialize
    public DonationSuccessEmailPayload() {}

    // Getters and Setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
}