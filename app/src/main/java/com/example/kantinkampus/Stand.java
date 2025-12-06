package com.example.kantinkampus;

public class Stand {
    private int id;
    private int ownerId; // seller_id
    private String nama;
    private String deskripsi;
    private String image;
    private String createdAt;

    public Stand() {}

    public Stand(int id, String nama, String deskripsi, String image, int ownerId) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.image = image;
        this.ownerId = ownerId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Stand{id=" + id + ", nama='" + nama + "'}";
    }
}