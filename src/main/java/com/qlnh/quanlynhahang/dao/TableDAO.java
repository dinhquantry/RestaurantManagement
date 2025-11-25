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
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int tableId, String status) {
        String sql = "UPDATE Tables SET status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}