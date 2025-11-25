package com.qlnh.quanlynhahang.dao;


import com.qlnh.quanlynhahang.model.Order;
import com.qlnh.quanlynhahang.util.DatabaseConnection;

import java.sql.*;

public class OrderDAO {

    // Tạo hóa đơn mới và trả về ID của hóa đơn đó
    public int createOrder(Order order) {
        String sql = "INSERT INTO Orders (table_id, user_id, order_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (order.getTableId() != null) stmt.setInt(1, order.getTableId());
            else stmt.setNull(1, Types.INTEGER);

            stmt.setInt(2, order.getUserId());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setDouble(4, 0.0); // Mới tạo thì tổng tiền là 0
            stmt.setString(5, "PENDING");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Trả về Order ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Lỗi
    }

    // Cập nhật tổng tiền cho hóa đơn
    public boolean updateTotalAmount(int orderId, double total) {
        String sql = "UPDATE Orders SET total_amount = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, total);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thanh toán hóa đơn
    public boolean payOrder(int orderId) {
        String sql = "UPDATE Orders SET status = 'PAID' WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // HÀM MỚI: Tìm ID hóa đơn đang phục vụ tại bàn (Status = PENDING)
    public int getPendingOrderId(int tableId) {
        String sql = "SELECT order_id FROM Orders WHERE table_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("order_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // -1 nghĩa là bàn này chưa có đơn nào, hoặc đang trống
    }
}