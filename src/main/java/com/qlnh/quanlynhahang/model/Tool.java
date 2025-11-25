package com.qlnh.quanlynhahang.model;

public class Tool {
    private int id;
    private String name;
    private int quantity;
    private int minThreshold; // Tên biến là minThreshold
    private String status;

    public Tool() {}

    public Tool(int id, String name, int quantity, int minThreshold, String status) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // ĐÂY LÀ HÀM QUAN TRỌNG ĐANG BỊ LỖI
    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}