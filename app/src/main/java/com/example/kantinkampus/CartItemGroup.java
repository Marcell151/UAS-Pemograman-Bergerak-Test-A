package com.example.kantinkampus;

import java.util.List;

/**
 * Cart Item Group Model
 * Groups cart items by stand
 */
class CartItemGroup {
    private int standId;
    private String standName;
    private List<CartItem> items;
    private int subtotal;

    public int getStandId() {
        return standId;
    }

    public void setStandId(int standId) {
        this.standId = standId;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }
}
