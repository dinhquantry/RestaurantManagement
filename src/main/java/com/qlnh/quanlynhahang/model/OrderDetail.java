package com.qlnh.quanlynhahang.model;

public class OrderDetail {
    private int id;
    private int orderId;
    private int foodId;
    private String foodName; // Field tạm, lấy từ bảng Foods qua JOIN
    private int quantity;
    private double priceAtOrder; // Giá tại thời điểm đặt

    public OrderDetail() {}

    public OrderDetail(int id, int orderId, int foodId, String foodName, int quantity, double priceAtOrder) {
        this.id = id;
        this.orderId = orderId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    // Tính thành tiền của dòng này (Số lượng * Giá)
    public double getTotalPrice() {
        return quantity * priceAtOrder;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(double priceAtOrder) { this.priceAtOrder = priceAtOrder; }
}