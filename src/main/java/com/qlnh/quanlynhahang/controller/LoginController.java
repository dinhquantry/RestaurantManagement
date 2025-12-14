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
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;       // Ô mật khẩu ẩn (dấu chấm)
    @FXML private TextField txtPasswordVisible;    // Ô mật khẩu hiện (chữ thường)
    @FXML private CheckBox chkShowPassword;        // Nút tick
    @FXML private ImageView imgLogo;

    private final UserDAO userDAO = new UserDAO();

    public void initialize() {
        // 1. Load Logo
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/logo.png");
            if (iconStream != null) {
                imgLogo.setImage(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load logo login: " + e.getMessage());
        }

        // 2. Đồng bộ dữ liệu giữa 2 ô mật khẩu
        // Khi gõ vào ô ẩn -> cập nhật ô hiện
        txtPassword.textProperty().bindBidirectional(txtPasswordVisible.textProperty());

        // Mặc định ẩn ô password text
        txtPasswordVisible.setVisible(false);
    }

    @FXML
    private void handleTogglePassword() {
        if (chkShowPassword.isSelected()) {
            // Hiện mật khẩu: Hiện TextField, Ẩn PasswordField
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);

            txtPassword.setVisible(false);
            txtPassword.setManaged(false); // Không chiếm chỗ layout
        } else {
            // Ẩn mật khẩu: Ngược lại
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);

            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        // Lấy mật khẩu từ ô nào cũng được vì chúng đã bind với nhau
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        User currentUser = userDAO.checkLogin(user, pass);

        if (currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
                Parent root = loader.load();

                MainDashboardController dashboard = loader.getController();
                dashboard.initData(currentUser);
                StaffManagementController.setCurrentLoggedInUserId(currentUser.getId());

                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 1200, 600)); // Kích thước Dashboard rộng hơn chút
                stage.setTitle("Quản lý nhà hàng - Xin chào " + currentUser.getFullName());
                stage.centerOnScreen();
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Lỗi", "Không thể tải màn hình chính!");
            }
        } else {
            AlertUtils.showError("Đăng nhập thất bại", "Sai tài khoản hoặc mật khẩu!\n(Hoặc tài khoản đã bị khóa)");
        }
    }
}