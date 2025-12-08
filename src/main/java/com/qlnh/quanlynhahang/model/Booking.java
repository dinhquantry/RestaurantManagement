package com.qlnh.quanlynhahang.model;

import java.sql.Timestamp;

public class Booking {
    private int id;
    private String customerName;
    private String phone;
    private int tableId;
    private String tableName; // Field phụ để hiển thị tên bàn thay vì ID
    private Timestamp bookingTime;
    private String status;
    private int guestCount; // Số lượng khách

    public Booking() {}

    public Booking(int id, String customerName, String phone, int tableId, Timestamp bookingTime, String status, int guestCount) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.tableId = tableId;
        this.bookingTime = bookingTime;
        this.status = status;
        this.guestCount = guestCount;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }
}