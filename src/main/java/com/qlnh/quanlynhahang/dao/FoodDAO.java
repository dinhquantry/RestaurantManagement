package com.qlnh.quanlynhahang.dao;


import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    public List<Food> getAllFoods() {
        List<Food> list = new ArrayList<>();
        String sql = "SELECT * FROM Foods";
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
        String sql = "INSERT INTO Foods (food_name, price, category, status, image_path) VALUES (?, ?, ?, ?, ?)";
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

    public boolean deleteFood(int id) {
        String sql = "DELETE FROM Foods WHERE food_id=?";
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