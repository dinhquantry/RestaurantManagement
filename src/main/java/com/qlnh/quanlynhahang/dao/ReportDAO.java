package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    public Map<String, Double> getRevenue(String type, Date fromDate, Date toDate) {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "";

        // SỬA LỖI QUAN TRỌNG:
        // Dùng CAST(order_date AS DATE) để loại bỏ phần giờ phút giây khi so sánh
        String whereClause = "WHERE status = 'PAID' AND CAST(order_date AS DATE) BETWEEN ? AND ? ";

        switch (type) {
            case "Ngày":
                sql = "SELECT FORMAT(order_date, 'dd/MM/yyyy') as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY CAST(order_date AS DATE), FORMAT(order_date, 'dd/MM/yyyy') " +
                        "ORDER BY CAST(order_date AS DATE) ASC";
                break;
            case "Tuần":
                sql = "SELECT CONCAT(N'Tuần ', DATEPART(ww, order_date), '/', YEAR(order_date)) as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY YEAR(order_date), DATEPART(ww, order_date) " +
                        "ORDER BY YEAR(order_date), DATEPART(ww, order_date) ASC";
                break;
            case "Tháng":
                sql = "SELECT FORMAT(order_date, 'MM/yyyy') as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY YEAR(order_date), MONTH(order_date), FORMAT(order_date, 'MM/yyyy') " +
                        "ORDER BY YEAR(order_date), MONTH(order_date) ASC";
                break;
            case "Năm":
                sql = "SELECT CAST(YEAR(order_date) as VARCHAR) as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY YEAR(order_date) " +
                        "ORDER BY YEAR(order_date) ASC";
                break;
        }

        // In ra console để kiểm tra (Debug)
        System.out.println("Đang chạy báo cáo: " + type);
        System.out.println("Từ: " + fromDate + " Đến: " + toDate);
        System.out.println("SQL: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, fromDate);
            stmt.setDate(2, toDate);

            ResultSet rs = stmt.executeQuery();
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String label = rs.getString("label");
                Double total = rs.getDouble("total");
                data.put(label, total);
                System.out.println("--> Tìm thấy: " + label + " = " + total);
            }

            if (!hasData) {
                System.out.println("--> KHÔNG tìm thấy dữ liệu nào trong khoảng này!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM Orders WHERE status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}