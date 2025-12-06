package com.example.kantinkampus;

public class History {
    private int id;
    private String items;
    private int total;
    private String tanggal;

    public History() {}

    public History(int id, String items, int total, String tanggal) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.tanggal = tanggal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }
}