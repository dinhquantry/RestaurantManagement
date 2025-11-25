package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.DiningTable;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public List<DiningTable> getAllTables() {
        List<DiningTable> list = new ArrayList<>();
        String sql = "SELECT * FROM Tables";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DiningTable(
                        rs.getInt("table_id"),
                        rs.getString("table_name"),
                        rs.getInt("capacity"),
                        rs.getString("status"),
                        rs.getInt("floor") // Lấy cột floor
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addTable(DiningTable t) {
        String sql = "INSERT INTO Tables (table_name, capacity, floor, status) VALUES (?, ?, ?, 'EMPTY')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getName());
            stmt.setInt(2, t.getCapacity());
            stmt.setInt(3, t.getFloor());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateTableInfo(DiningTable t) {
        String sql = "UPDATE Tables SET table_name = ?, capacity = ?, floor = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getName());
            stmt.setInt(2, t.getCapacity());
            stmt.setInt(3, t.getFloor());
            stmt.setInt(4, t.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteTable(int id) {
        // Chỉ xóa được nếu bàn đang TRỐNG
        String sql = "DELETE FROM Tables WHERE table_id = ? AND status = 'EMPTY'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Hàm cập nhật trạng thái (Giữ nguyên)
    public boolean updateStatus(int tableId, String status) {
        String sql = "UPDATE Tables SET status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}