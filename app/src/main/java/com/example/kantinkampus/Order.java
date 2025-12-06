package com.example.kantinkampus;

public class Order {
    private int id;
    private String userName;
    private String standName;
    private String items;
    private int total;
    private String status;
    private String createdAt;
    private String paymentMethod;
    private String notes;

    // Status constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    public Order() {}

    public Order(int id, String userName, String standName, String items, int total, String status, String createdAt, String paymentMethod, String notes) {
        this.id = id;
        this.userName = userName;
        this.standName = standName;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Status helper methods
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(status);
    }

    public boolean isReady() {
        return STATUS_READY.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public String getStatusText() {
        switch (status) {
            case STATUS_PENDING:
                return "Menunggu Konfirmasi";
            case STATUS_PROCESSING:
                return "Sedang Diproses";
            case STATUS_READY:
                return "Siap Diambil";
            case STATUS_COMPLETED:
                return "Selesai";
            case STATUS_CANCELLED:
                return "Dibatalkan";
            default:
                return "Unknown";
        }
    }

    public String getStatusEmoji() {
        switch (status) {
            case STATUS_PENDING:
                return "⏳";
            case STATUS_PROCESSING:
                return "👨‍🍳";
            case STATUS_READY:
                return "✅";
            case STATUS_COMPLETED:
                return "🎉";
            case STATUS_CANCELLED:
                return "❌";
            default:
                return "❓";
        }
    }
}