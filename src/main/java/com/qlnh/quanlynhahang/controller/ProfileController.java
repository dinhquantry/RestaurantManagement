package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.UserDAO;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ProfileController {

    @FXML private ImageView imgAvatar;
    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtRole;
    @FXML private TextField txtPhone;
    
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;

    private User currentUser;
    private final UserDAO userDAO = new UserDAO();
    private String currentAvatarPath;

    // Hàm nhận User từ Dashboard
    public void initData(User user) {
        this.currentUser = user;
        fillData();
    }

    private void fillData() {
        if (currentUser == null) return;

        txtFullName.setText(currentUser.getFullName());
        txtUsername.setText(currentUser.getUsername());
        txtRole.setText(currentUser.getRole());
        txtPhone.setText(currentUser.getPhone());
        
        currentAvatarPath = currentUser.getAvatarPath();
        loadAvatar();
    }

    private void loadAvatar() {
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            File file = new File(currentAvatarPath);
            if (file.exists()) {
                imgAvatar.setImage(new Image(file.toURI().toString()));
            }
        } else {
            // Có thể set ảnh mặc định nếu muốn
            imgAvatar.setImage(null); 
        }
    }

    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(txtFullName.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File destDir = new File("data/images");
                if (!destDir.exists()) destDir.mkdirs();

                String newFileName = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(destDir, newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                currentAvatarPath = "data/images/" + newFileName;
                loadAvatar();
                
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Lỗi", "Không thể upload ảnh!");
            }
        }
    }

    @FXML
    private void handleSave() {
        // 1. Cập nhật thông tin cơ bản
        currentUser.setPhone(txtPhone.getText());
        currentUser.setAvatarPath(currentAvatarPath);

        // 2. Kiểm tra đổi mật khẩu
        String newPass = txtNewPass.getText();
        String confirmPass = txtConfirmPass.getText();

        if (!newPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                AlertUtils.showError("Lỗi", "Mật khẩu xác nhận không khớp!");
                return;
            }
            currentUser.setPassword(newPass);
        } else {
            // Nếu không nhập pass mới thì giữ nguyên (Model User đang lưu pass cũ hoặc rỗng tùy logic)
            // Trong UserDAO logic update sẽ kiểm tra null/empty để quyết định có update pass không
            // Ở đây ta set lại password rỗng để UserDAO biết là không cần update password
            currentUser.setPassword(""); 
        }

        // 3. Gọi DAO lưu xuống DB
        if (userDAO.updateUser(currentUser)) {
            AlertUtils.showInfo("Thành công", "Đã cập nhật hồ sơ cá nhân!");
            txtNewPass.clear();
            txtConfirmPass.clear();
        } else {
            AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
        }
    }
}