package com.qlnh.quanlynhahang.model;

import java.sql.Date;

public class Shift {
    private int id;
    private int userId;
    private String employeeName; // Field hiển thị (lấy từ bảng Users)
    private Date shiftDate;
    private String shiftName;
    private String note;

    public Shift(int id, int userId, String employeeName, Date shiftDate, String shiftName, String note) {
        this.id = id;
        this.userId = userId;
        this.employeeName = employeeName;
        this.shiftDate = shiftDate;
        this.shiftName = shiftName;
        this.note = note;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getEmployeeName() { return employeeName; }
    public Date getShiftDate() { return shiftDate; }
    public String getShiftName() { return shiftName; }
    public String getNote() { return note; }
}