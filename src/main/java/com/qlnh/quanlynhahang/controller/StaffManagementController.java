package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.UserDAO;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class StaffManagementController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cbRole;
    @FXML private ImageView imgAvatar;

    @FXML private TableView<User> tblStaff;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;

    private final UserDAO userDAO = new UserDAO();
    private ObservableList<User> staffList = FXCollections.observableArrayList();
    private String currentAvatarPath = null;

    public void initialize() {
        // Init ComboBox
        cbRole.setItems(FXCollections.observableArrayList("MANAGER", "STAFF"));
        cbRole.getSelectionModel().select("STAFF");

        // Init Table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        loadData();

        tblStaff.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });
    }

    private void loadData() {
        List<User> list = userDAO.getAllStaff();
        staffList.setAll(list);
        tblStaff.setItems(staffList);
    }

    private void fillForm(User u) {
        txtUsername.setText(u.getUsername());
        txtUsername.setDisable(true); // Không cho sửa username
        txtPassword.clear(); // Không hiện password cũ
        txtFullName.setText(u.getFullName());
        txtPhone.setText(u.getPhone());
        cbRole.setValue(u.getRole());

        currentAvatarPath = u.getAvatarPath();
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            File file = new File(currentAvatarPath);
            if (file.exists()) imgAvatar.setImage(new Image(file.toURI().toString()));
            else imgAvatar.setImage(null);
        } else {
            imgAvatar.setImage(null);
        }
    }

    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                File destDir = new File("data/images");
                if (!destDir.exists()) destDir.mkdirs();

                String newFileName = "avatar_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, newFileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                currentAvatarPath = "data/images/" + newFileName;
                imgAvatar.setImage(new Image(destFile.toURI().toString()));
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Lỗi", "Upload ảnh thất bại!");
            }
        }
    }

    @FXML
    private void handleAdd() {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập Username và Password!");
            return;
        }

        User u = new User();
        u.setUsername(txtUsername.getText());
        u.setPassword(txtPassword.getText());
        u.setFullName(txtFullName.getText());
        u.setRole(cbRole.getValue());
        u.setPhone(txtPhone.getText());
        u.setAvatarPath(currentAvatarPath);

        if (userDAO.addUser(u)) {
            AlertUtils.showInfo("Thành công", "Thêm nhân viên mới thành công!");
            loadData();
            handleClear();
        } else {
            AlertUtils.showError("Lỗi", "Thêm thất bại (Có thể trùng Username)!");
        }
    }

    @FXML
    private void handleUpdate() {
        User selected = tblStaff.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setFullName(txtFullName.getText());
        selected.setRole(cbRole.getValue());
        selected.setPhone(txtPhone.getText());
        selected.setAvatarPath(currentAvatarPath);
        // Nếu người dùng nhập pass mới thì mới cập nhật
        if (!txtPassword.getText().isEmpty()) {
            selected.setPassword(txtPassword.getText());
        }

        if (userDAO.updateUser(selected)) {
            AlertUtils.showInfo("Thành công", "Cập nhật thành công!");
            loadData();
            tblStaff.refresh();
        } else {
            AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
        }
    }

    @FXML
    private void handleDelete() {
        User selected = tblStaff.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (AlertUtils.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa tài khoản: " + selected.getUsername() + "?")) {
            if (userDAO.deleteUser(selected.getId())) {
                AlertUtils.showInfo("Thành công", "Đã xóa!");
                loadData();
                handleClear();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtUsername.clear();
        txtUsername.setDisable(false);
        txtPassword.clear();
        txtFullName.clear();
        txtPhone.clear();
        cbRole.getSelectionModel().select("STAFF");
        imgAvatar.setImage(null);
        currentAvatarPath = null;
        tblStaff.getSelectionModel().clearSelection();
    }
}