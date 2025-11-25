package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.Shift;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {

    // Lấy danh sách ca làm (JOIN với bảng Users để lấy tên)
    public List<Shift> getAllShifts() {
        List<Shift> list = new ArrayList<>();
        // Chỉ lấy nhân viên đang hoạt động (is_active = 1)
        String sql = "SELECT s.*, u.full_name FROM Shifts s JOIN Users u ON s.user_id = u.user_id WHERE u.is_active = 1 ORDER BY s.shift_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Shift(
                        rs.getInt("shift_id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getDate("shift_date"),
                        rs.getString("shift_name"),
                        rs.getString("note")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addShift(Shift shift) {
        String sql = "INSERT INTO Shifts (user_id, shift_date, shift_name, note) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shift.getUserId());
            stmt.setDate(2, shift.getShiftDate());
            stmt.setString(3, shift.getShiftName());
            stmt.setString(4, shift.getNote());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteShift(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM Shifts WHERE shift_id=?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}