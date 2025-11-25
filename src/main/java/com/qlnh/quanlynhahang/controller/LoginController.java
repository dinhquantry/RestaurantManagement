package com.qlnh.quanlynhahang.controller;


import com.qlnh.quanlynhahang.dao.UserDAO;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        User currentUser = userDAO.checkLogin(user, pass);

        if (currentUser != null) {
            try {
                // Chuyển sang màn hình chính
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
                Parent root = loader.load();

                // Truyền dữ liệu User sang Dashboard
                MainDashboardController dashboard = loader.getController();
                dashboard.initData(currentUser);

                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
                stage.setTitle("Quản lý nhà hàng - Xin chào " + currentUser.getFullName());
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Lỗi", "Không thể tải màn hình chính!");
            }
        } else {
            AlertUtils.showError("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu!");
        }
    }
}