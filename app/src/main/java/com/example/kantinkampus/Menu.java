package com.example.kantinkampus;

public class Menu {
    private int id;
    private int standId;
    private String nama;
    private int harga;
    private String image;
    private String deskripsi;
    private String kategori;
    private String status; // 'available' or 'unavailable'
    private String createdAt;

    // For display
    private float averageRating;
    private int totalReviews;

    public Menu() {}

    public Menu(int id, int standId, String nama, int harga, String image,
                String deskripsi, String kategori, String status) {
        this.id = id;
        this.standId = standId;
        this.nama = nama;
        this.harga = harga;
        this.image = image;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.status = status;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStandId() { return standId; }
    public void setStandId(int standId) { this.standId = standId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public boolean isAvailable() { return "available".equals(status); }

    public String getFormattedPrice() {
        return "Rp " + String.format("%,d", harga).replace(',', '.');
    }

    @Override
    public String toString() {
        return "Menu{id=" + id + ", nama='" + nama + "', harga=" + harga + "}";
    }
}
