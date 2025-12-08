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
        String sql = "SELECT b.*, t.table_name FROM Bookings b LEFT JOIN Tables t ON b.table_id = t.table_id ORDER BY b.booking_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapToBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO Bookings (customer_name, phone, table_id, booking_time, status, guest_count) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, booking.getCustomerName());
            stmt.setString(2, booking.getPhone());
            stmt.setInt(3, booking.getTableId());
            stmt.setTimestamp(4, booking.getBookingTime());
            stmt.setString(5, "CONFIRMED");
            stmt.setInt(6, booking.getGuestCount());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thông tin đặt bàn gần nhất của một bàn cụ thể
     */
    public Booking getLatestBooking(int tableId) {
        String sql = "SELECT TOP 1 b.*, t.table_name FROM Bookings b " +
                "LEFT JOIN Tables t ON b.table_id = t.table_id " +
                "WHERE b.table_id = ? AND b.status = 'CONFIRMED' " +
                "ORDER BY b.booking_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToBooking(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Booking mapToBooking(ResultSet rs) throws SQLException {
        int guestCount = 0;
        try {
            guestCount = rs.getInt("guest_count");
        } catch (SQLException e) {
            // Bỏ qua nếu cột chưa tồn tại
        }

        Booking booking = new Booking(
                rs.getInt("booking_id"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getInt("table_id"),
                rs.getTimestamp("booking_time"),
                rs.getString("status"),
                guestCount
        );

        try {
            booking.setTableName(rs.getString("table_name"));
        } catch (SQLException e) {
            // Bỏ qua lỗi
        }

        return booking;
    }
}