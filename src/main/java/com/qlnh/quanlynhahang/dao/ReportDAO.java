package com.qlnh.quanlynhahang.dao;

import com.qlnh.quanlynhahang.util.DatabaseConnection;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {

    // Hàm lấy doanh thu theo khoảng thời gian và tiêu chí gom nhóm
    public Map<String, Double> getRevenue(String type, Date fromDate, Date toDate) {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "";

        // Thêm điều kiện lọc ngày vào câu WHERE
        String whereClause = "WHERE status = 'PAID' AND order_date BETWEEN ? AND ? ";

        switch (type) {
            case "Ngày":
                // Gom nhóm theo ngày
                sql = "SELECT FORMAT(order_date, 'dd/MM/yyyy') as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY CAST(order_date AS DATE), FORMAT(order_date, 'dd/MM/yyyy') " +
                        "ORDER BY CAST(order_date AS DATE) ASC";
                break;

            case "Tuần":
                // Gom nhóm theo Tuần
                sql = "SELECT CONCAT(N'Tuần ', DATEPART(ww, order_date), '/', YEAR(order_date)) as label, SUM(total_amount) as total " +
                        "FROM Orders " + whereClause +
                        "GROUP BY YEAR(order_date), DATEPART(ww, order_date) " +
                        "ORDER BY YEAR(order_date), DATEPART(ww, order_date) ASC";
                break;

            case "Tháng":
                // Gom nhóm theo Tháng
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set tham số ngày cho câu truy vấn (Lưu ý: query nào cũng có 2 tham số ? cho BETWEEN)
            stmt.setDate(1, fromDate);
            // Cộng thêm 1 ngày cho toDate để bao gồm cả ngày cuối cùng (nếu dùng DATETIME)
            // hoặc giữ nguyên nếu dùng DATE. Ở đây ta dùng DATE nên giữ nguyên.
            stmt.setDate(2, toDate);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("label"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}