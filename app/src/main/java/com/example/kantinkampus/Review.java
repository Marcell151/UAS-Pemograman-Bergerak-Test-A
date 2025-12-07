package com.example.kantinkampus;

/**
 * REVIEW MODEL - Updated with buyerName
 */
public class Review {
    private int id;
    private int buyerId;
    private int menuId;
    private int orderId;
    private int rating;
    private String comment;
    private String createdAt;
    private String buyerName; // ADD THIS FIELD

    public Review() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", buyerId=" + buyerId +
                ", menuId=" + menuId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", buyerName='" + buyerName + '\'' +
                '}';
    }
}