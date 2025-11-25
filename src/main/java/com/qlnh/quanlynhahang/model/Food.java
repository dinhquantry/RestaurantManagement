package com.qlnh.quanlynhahang.model;

public class Food {
    private int id;
    private String name;
    private double price;
    private String category;
    private String status;      // AVAILABLE, SOLD_OUT
    private String imagePath;   // Có thể null

    public Food() {}

    public Food(int id, String name, double price, String category, String status, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.status = status;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // Helper để hiển thị trên ComboBox nếu cần
    @Override
    public String toString() {
        return name;
    }
}