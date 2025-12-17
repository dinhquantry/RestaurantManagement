package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. Sửa hàm checkLogin: Chỉ cho phép đăng nhập nếu tài khoản còn hoạt động (is_active = 1)
    public User checkLogin(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Sửa hàm getAllStaff: Chỉ lấy danh sách nhân viên đang hoạt động
    public List<User> getAllStaff() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM Users WHERE is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. Sửa hàm addUser: Mặc định is_active = 1 khi tạo mới
    public boolean addUser(User user) {
        // Lưu ý: Cần chạy lệnh SQL thêm cột is_active vào Database trước (xem hướng dẫn bên dưới)
        String query = "INSERT INTO Users (username, password, full_name, role, phone, avatar_path, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getAvatarPath());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();
        String query;
        if (updatePassword) {
            query = "UPDATE Users SET full_name=?, username=?, role=?, phone=?, avatar_path=?, password=? WHERE user_id=?";
        } else {
            query = "UPDATE Users SET full_name=?,username=?,  role=?, phone=?, avatar_path=? WHERE user_id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAvatarPath());

            if (updatePassword) {
                stmt.setString(6, user.getPassword());
                stmt.setInt(7, user.getId());
            } else {
                stmt.setInt(8, user.getId());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. QUAN TRỌNG: Sửa hàm deleteUser thành Soft Delete (Ẩn đi thay vì xóa)
    // Việc này giúp tránh lỗi FK Constraint khi nhân viên đã có dữ liệu hóa đơn
    public boolean deleteUser(int id) {
        String query = "UPDATE Users SET is_active = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("avatar_path"),
                rs.getString("phone"),
                rs.getTimestamp("created_at")
        );
    }
}