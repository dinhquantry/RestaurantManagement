package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    public List<Food> getAllFoods() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT * FROM Foods WHERE is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Food(
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("image_path")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addFood(Food food) {
        String sql = "INSERT INTO Foods (food_name, price, category, status, image_path, is_active) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, food.getName());
            stmt.setDouble(2, food.getPrice());
            stmt.setString(3, food.getCategory());
            stmt.setString(4, food.getStatus());
            stmt.setString(5, food.getImagePath());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFood(Food food) {
        String sql = "UPDATE Foods SET food_name=?, price=?, category=?, status=?, image_path=? WHERE food_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, food.getName());
            stmt.setDouble(2, food.getPrice());
            stmt.setString(3, food.getCategory());
            stmt.setString(4, food.getStatus());
            stmt.setString(5, food.getImagePath());
            stmt.setInt(6, food.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- LOGIC XÓA THÔNG MINH (HYBRID DELETE) ---
    public boolean deleteFood(int id) {
        // Bước 1: Kiểm tra xem món này đã từng được bán chưa (có trong OrderDetails không)
        String checkSql = "SELECT COUNT(*) FROM OrderDetails WHERE food_id = ?";
        boolean isSold = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                isSold = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Lỗi kết nối thì dừng luôn
        }

        // Bước 2: Quyết định phương án xóa
        String sql;
        if (isSold) {
            // Trường hợp A: Đã bán -> Chỉ được ẨN (Soft Delete) để giữ lịch sử doanh thu
            sql = "UPDATE Foods SET is_active = 0 WHERE food_id = ?";
            System.out.println("LOG: Món ăn đã có giao dịch -> Thực hiện Xóa mềm (Ẩn).");
        } else {
            // Trường hợp B: Chưa bán (nhập sai, dư thừa) -> XÓA THẬT (Hard Delete) cho sạch DB
            sql = "DELETE FROM Foods WHERE food_id = ?";
            System.out.println("LOG: Món ăn chưa có giao dịch -> Thực hiện Xóa cứng (Vĩnh viễn).");
        }

        // Bước 3: Thực thi lệnh SQL đã chọn
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}