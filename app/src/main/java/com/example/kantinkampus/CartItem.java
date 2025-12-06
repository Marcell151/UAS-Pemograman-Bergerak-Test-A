package com.example.kantinkampus;

public class CartItem {
    private int id;
    private Menu menu;
    private int qty;

    public CartItem() {}

    public CartItem(int id, Menu menu, int qty) {
        this.id = id;
        this.menu = menu;
        this.qty = qty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getSubtotal() {
        return menu.getHarga() * qty;
    }
}