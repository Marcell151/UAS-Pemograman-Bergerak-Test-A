package com.example.kantinkampus;

public class OrderItem {
    private int id;
    private int orderId;
    private int menuId;
    private String menuName;
    private int qty;
    private int price;

    public OrderItem() {}

    public OrderItem(int id, int orderId, int menuId, String menuName, int qty, int price) {
        this.id = id;
        this.orderId = orderId;
        this.menuId = menuId;
        this.menuName = menuName;
        this.qty = qty;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSubtotal() {
        return price * qty;
    }
}