package com.example.token.model;

import java.util.Date;

public class PaymentEntry {
    private int amount;
    private String paymentId;
    private String status;
    private Date timestamp;

    public PaymentEntry() {}

    public PaymentEntry(int amount, String paymentId, String status, Date timestamp) {
        this.amount = amount;
        this.paymentId = paymentId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
