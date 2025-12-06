package com.example.kantinkampus;

public class OrderItem {
    private int id;
    private int orderId;
    private int menuId;
    private int qty;
    private int price;
    private int subtotal;

    // For display
    private String menuName;

    public OrderItem() {}

    public OrderItem(int menuId, String menuName, int qty, int price, int subtotal) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.qty = qty;
        this.price = price;
        this.subtotal = subtotal;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getFormattedPrice() {
        return "Rp " + String.format("%,d", price).replace(',', '.');
    }

    public String getFormattedSubtotal() {
        return "Rp " + String.format("%,d", subtotal).replace(',', '.');
    }

    @Override
    public String toString() {
        return "OrderItem{menuName='" + menuName + "', qty=" + qty + ", subtotal=" + subtotal + "}";
    }
}
