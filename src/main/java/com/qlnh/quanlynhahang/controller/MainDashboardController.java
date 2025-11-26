package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label lblWelcome;
    @FXML private VBox adminBox;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        lblWelcome.setText("Xin chào: " + user.getFullName() + " (" + user.getRole() + ")");

        applyPermissions();

        // Mặc định load trang sơ đồ bàn đầu tiên
        handleShowTables();
    }

    private void applyPermissions() {
        // Nếu là STAFF thì ẩn toàn bộ nhóm nút Admin (adminBox)
        if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
            adminBox.setVisible(false);
            adminBox.setManaged(false);
        }
    }

    private void switchView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFileName));
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi Giao diện", "Không thể tải màn hình: " + fxmlFileName + "\nLỗi: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowTables() {
        // CẬP NHẬT: Truyền currentUser sang TableMapController để sửa lỗi Foreign Key
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TableMapView.fxml"));
            Parent view = loader.load();

            TableMapController controller = loader.getController();
            controller.initData(currentUser);

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể tải Sơ đồ bàn!");
        }
    }

    @FXML
    private void handleShowOrder() {
        // CẬP NHẬT: Truyền User sang OrderController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderView.fxml"));
            Parent view = loader.load();

            OrderController controller = loader.getController();
            controller.initData(currentUser);

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở màn hình Gọi món!");
        }
    }

    @FXML
    private void handleShowTools() {
        switchView("ToolView.fxml");
    }

    // Nút mới: Quản lý thực đơn
    @FXML
    private void handleManageMenu() {
        switchView("MenuManagement.fxml");
    }

    @FXML
    private void handleManageStaff() {
        switchView("StaffManagement.fxml");
    }

    // Nút mới: Báo cáo Doanh thu (Đã thêm)
    @FXML
    private void handleShowReport() {
        switchView("ReportView.fxml");
    }

    @FXML
    private void handleShowShifts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShiftManagement.fxml"));
            Parent view = loader.load();

            // Lấy controller và truyền user sang
            ShiftManagementController controller = loader.getController();
            controller.initData(currentUser);

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("Quản lý Nhà hàng - Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}