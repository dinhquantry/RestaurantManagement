package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.Tool;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ToolDAO {

    public List<Tool> getAllTools() {
        List<Tool> list = new ArrayList<>();
        String sql = "SELECT * FROM Tools";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Tool(
                        rs.getInt("tool_id"),
                        rs.getString("tool_name"),
                        rs.getInt("quantity"),
                        rs.getInt("min_threshold"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm Thêm mới
    public boolean addTool(Tool tool) {
        String sql = "INSERT INTO Tools (tool_name, quantity, min_threshold, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tool.getName());
            stmt.setInt(2, tool.getQuantity());
            stmt.setInt(3, tool.getMinThreshold());
            stmt.setString(4, tool.getStatus() != null ? tool.getStatus() : "Tốt");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm Cập nhật
    public boolean updateTool(Tool tool) {
        String sql = "UPDATE Tools SET tool_name=?, quantity=?, min_threshold=?, status=? WHERE tool_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tool.getName());
            stmt.setInt(2, tool.getQuantity());
            stmt.setInt(3, tool.getMinThreshold());
            stmt.setString(4, tool.getStatus());
            stmt.setInt(5, tool.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm Xóa
    public boolean deleteTool(int id) {
        String sql = "DELETE FROM Tools WHERE tool_id=?";
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