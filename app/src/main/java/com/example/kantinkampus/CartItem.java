package com.example.kantinkampus;

public class CartItem {
    private int id;
    private int userId;
    private int menuId;
    private int qty;
    private String notes;

    // For display
    private Menu menu;

    public CartItem() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }

    public int getSubtotal() {
        return menu != null ? menu.getHarga() * qty : 0;
    }

    public String getFormattedSubtotal() {
        int subtotal = getSubtotal();
        return "Rp " + String.format("%,d", subtotal).replace(',', '.');
    }

    @Override
    public String toString() {
        return "CartItem{qty=" + qty + ", subtotal=" + getSubtotal() + "}";
    }
}
