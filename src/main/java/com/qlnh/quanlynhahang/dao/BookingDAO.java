package com.qlnh.quanlynhahang.dao;
import com.qlnh.quanlynhahang.model.Booking;
import com.qlnh.quanlynhahang.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Lấy danh sách booking kèm tên bàn (JOIN)
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, t.table_name FROM Bookings b LEFT JOIN Tables t ON b.table_id = t.table_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("booking_time"),
                        rs.getString("status")
                );
                booking.setTableName(rs.getString("table_name")); // Set thêm tên bàn để hiển thị
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO Bookings (customer_name, phone, table_id, booking_time, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, booking.getCustomerName());
            stmt.setString(2, booking.getPhone());
            stmt.setInt(3, booking.getTableId());
            stmt.setTimestamp(4, booking.getBookingTime());
            stmt.setString(5, "CONFIRMED");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}