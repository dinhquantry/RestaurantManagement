package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class MainDashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label lblWelcome;
    @FXML private ImageView imgAvatar; // Thêm biến để liên kết với ImageView trong FXML
    @FXML private VBox adminBox;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        lblWelcome.setText("Xin chào: " + user.getFullName() + " (" + user.getRole() + ")");

        // Tải ảnh đại diện lên Dashboard
        loadUserAvatar();

        applyPermissions();

        // Mặc định load trang sơ đồ bàn đầu tiên
        handleShowTables();
    }

    private void loadUserAvatar() {
        // Kiểm tra xem ImageView có tồn tại trong FXML không và đường dẫn ảnh có hợp lệ không
        if (imgAvatar != null && currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) {
            File file = new File(currentUser.getAvatarPath());
            if (file.exists()) {
                try {
                    imgAvatar.setImage(new Image(file.toURI().toString()));
                } catch (Exception e) {
                    System.err.println("Lỗi load ảnh dashboard: " + e.getMessage());
                }
            }
        }
    }

    private void applyPermissions() {
        if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
            adminBox.setVisible(false);
            adminBox.setManaged(false);
        }
    }

    /**
     * Hàm đa năng để chuyển đổi view và cấu hình controller
     * @param fxmlFileName Tên file FXML cần load
     * @param controllerSetup Hàm lambda để xử lý controller (có thể null nếu không cần)
     */
    private void loadView(String fxmlFileName, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFileName));
            Parent view = loader.load();

            // Nếu có logic khởi tạo controller (ví dụ truyền user), thực thi nó
            if (controllerSetup != null) {
                controllerSetup.accept(loader.getController());
            }

            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi Giao diện", "Không thể tải màn hình: " + fxmlFileName + "\nLỗi: " + e.getMessage());
        }
    }

    // Hàm overload cho các view đơn giản không cần setup controller
    private void loadView(String fxmlFileName) {
        loadView(fxmlFileName, null);
    }

    // --- CÁC CHỨC NĂNG CHUNG (ALL USERS) ---

    @FXML
    private void handleShowTables() {
        loadView("TableMapView.fxml", controller -> {
            TableMapController c = (TableMapController) controller;
            c.initData(currentUser);
        });
    }

    @FXML
    private void handleShowOrder() {
        loadView("OrderView.fxml", controller -> {
            OrderController c = (OrderController) controller;
            c.initData(currentUser);
        });
    }

    @FXML
    private void handleShowTools() {
        loadView("ToolView.fxml");
    }

    @FXML
    private void handleShowShifts() {
        loadView("ShiftManagement.fxml", controller -> {
            ShiftManagementController c = (ShiftManagementController) controller;
            c.initData(currentUser);
        });
    }

    @FXML
    private void handleShowProfile() {
        loadView("ProfileView.fxml", controller -> {
            ProfileController c = (ProfileController) controller;
            c.initData(currentUser);
        });
    }

    // --- CÁC CHỨC NĂNG QUẢN TRỊ (ADMIN ONLY) ---

    @FXML
    private void handleManageMenu() {
        loadView("MenuManagement.fxml");
    }

    @FXML
    private void handleManageStaff() {
        loadView("StaffManagement.fxml");
    }

    @FXML
    private void handleShowReport() {
        loadView("ReportView.fxml");
    }

    // --- HỆ THỐNG ---

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
            AlertUtils.showError("Lỗi", "Không thể đăng xuất!");
        }
    }
}