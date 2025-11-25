package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.OrderDetail;
import com.qlnh.quanlynhahang.model.OrderItem;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {

    // Thêm chi tiết hóa đơn vào DB
    public boolean addOrderDetail(OrderDetail detail) {
        String sql = "INSERT INTO OrderDetails (order_id, food_id, quantity, price_at_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setInt(2, detail.getFoodId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPriceAtOrder());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa chi tiết cũ để cập nhật lại (tránh trùng lặp khi lưu nhiều lần)
    public void clearDetails(int orderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM OrderDetails WHERE order_id = ?")) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Lấy danh sách món ăn đã gọi của một đơn hàng (Để hiển thị lại khi mở bàn cũ)
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();

        // Lấy thêm cột price_at_order để điền vào unitPrice
        String sql = "SELECT d.food_id, f.food_name, d.quantity, d.price_at_order, (d.quantity * d.price_at_order) as total " +
                "FROM OrderDetails d JOIN Foods f ON d.food_id = f.food_id " +
                "WHERE d.order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Sử dụng Constructor đầy đủ của OrderItem (có unitPrice)
                list.add(new OrderItem(
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_order"), // Đơn giá gốc lúc gọi
                        rs.getDouble("total")           // Thành tiền
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}