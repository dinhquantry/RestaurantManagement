package com.qlnh.quanlynhahang.model;

public class OrderItem {
    private int foodId;
    private String foodName;
    private int quantity;
    private double unitPrice; // Thêm trường này để lưu giá gốc
    private double totalPrice;

    public OrderItem(int foodId, String foodName, int quantity, double unitPrice, double totalPrice) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Constructor cũ (để tương thích nếu cần, nhưng nên dùng cái trên)
    public OrderItem(int foodId, String foodName, int quantity, double totalPrice) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.unitPrice = quantity > 0 ? totalPrice / quantity : 0;
    }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; } // Getter mới
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}