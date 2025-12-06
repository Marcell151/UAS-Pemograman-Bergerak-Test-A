package com.example.kantinkampus;

public class Menu {
    private int id;
    private int standId;
    private String nama;
    private int harga;
    private String deskripsi;
    private String kategori;
    private String image;
    private boolean available;
    private double averageRating;
    private int totalReviews;

    public Menu() {
        this.available = true;
        this.averageRating = 0.0;
        this.totalReviews = 0;
    }

    public Menu(int id, int standId, String nama, int harga) {
        this.id = id;
        this.standId = standId;
        this.nama = nama;
        this.harga = harga;
        this.available = true;
        this.averageRating = 0.0;
        this.totalReviews = 0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStandId() {
        return standId;
    }

    public void setStandId(int standId) {
        this.standId = standId;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }
}