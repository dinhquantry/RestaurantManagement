package com.qlnh.quanlynhahang.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Cấu hình cho SQL Server
    // 1433 là cổng mặc định.
    // encrypt=true;trustServerCertificate=true; là bắt buộc với các phiên bản SQL Server mới để tránh lỗi SSL
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=RestaurantDB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa"; // Tài khoản mặc định của SQL Server thường là 'sa'
    private static final String PASS = "1"; // Mật khẩu bạn đặt lúc cài SQL Server

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load driver của SQL Server (mssql-jdbc)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Kết nối SQL Server thất bại! Kiểm tra lại User/Pass hoặc TCP/IP đã bật chưa.");
        }
        return conn;
    }
}