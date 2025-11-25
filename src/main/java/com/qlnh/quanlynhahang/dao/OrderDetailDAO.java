package com.qlnh.quanlynhahang.dao;


import com.qlnh.quanlynhahang.model.OrderDetail;
import com.qlnh.quanlynhahang.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {

    // Thêm món vào hóa đơn
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

    // Lấy danh sách món của một hóa đơn (kèm tên món)
    public List<OrderDetail> getDetailsByOrderId(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT d.*, f.food_name FROM OrderDetails d JOIN Foods f ON d.food_id = f.food_id WHERE d.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new OrderDetail(
                        rs.getInt("detail_id"),
                        rs.getInt("order_id"),
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_order")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}