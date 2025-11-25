package com.qlnh.quanlynhahang.model;

public class DiningTable {
    private int id;
    private String name;
    private int capacity;
    private String status; // EMPTY, OCCUPIED, BOOKED
    private int floor;     // MỚI: Tầng

    public DiningTable() {}

    public DiningTable(int id, String name, int capacity, String status, int floor) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.floor = floor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    @Override
    public String toString() { return name; }
}