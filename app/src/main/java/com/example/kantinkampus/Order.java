package com.example.kantinkampus;

import java.util.List;

public class Order {
    private int id;
    private int userId; // buyer_id
    private int standId;
    private int total;
    private String status;
    private String paymentMethod;
    private String paymentProof;
    private String paymentStatus;
    private String sellerNotes;
    private String notes; // buyer_notes
    private String createdAt;
    private String updatedAt;

    // For display
    private String standName;
    private String userName;
    private List<OrderItem> items;

    public Order() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStandId() { return standId; }
    public void setStandId(int standId) { this.standId = standId; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentProof() { return paymentProof; }
    public void setPaymentProof(String paymentProof) { this.paymentProof = paymentProof; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getSellerNotes() { return sellerNotes; }
    public void setSellerNotes(String sellerNotes) { this.sellerNotes = sellerNotes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getFormattedTotal() {
        return "Rp " + String.format("%,d", total).replace(',', '.');
    }

    public String getStatusDisplay() {
        switch (status) {
            case "pending_payment":
                return "Menunggu Pembayaran";
            case "pending_verification":
                return "Menunggu Verifikasi";
            case "verified":
                return "Diverifikasi";
            case "cooking":
                return "Sedang Dimasak";
            case "ready":
                return "Siap Diambil";
            case "completed":
                return "Selesai";
            case "cancelled":
                return "Dibatalkan";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", total=" + total + ", status='" + status + "'}";
    }
}