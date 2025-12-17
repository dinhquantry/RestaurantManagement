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
    @FXML private TextField txtPassword;
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

    // Giả sử bạn lưu ID người đang đăng nhập vào đây.
    // Bạn cần set giá trị này khi chuyển từ màn hình Login sang màn hình này.
    // Hoặc lấy từ Session global ví dụ: Session.currentUser.getId()
    private static int currentLoggedInUserId = 1;

    // Hàm để set user hiện tại từ bên ngoài (ví dụ từ MainController)
    public static void setCurrentLoggedInUserId(int id) {
        currentLoggedInUserId = id;
    }

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

        // 1.  Cho phép sửa Username

        txtPassword.clear();
        txtFullName.setText(u.getFullName());
        txtPassword.setText(u.getPassword());
        txtPhone.setText(u.getPhone());
        cbRole.setValue(u.getRole());

        // 2. MỚI: Kiểm tra nếu user đang chọn là chính mình thì KHÔNG cho sửa Role
        if (u.getId() == currentLoggedInUserId) {
            cbRole.setDisable(true); // Khóa combobox
        } else {
            cbRole.setDisable(false); // Mở khóa
        }

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
        // ... (Giữ nguyên code upload ảnh cũ)
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
        // ... (Giữ nguyên code handleAdd cũ)
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
        if (selected == null) {
            AlertUtils.showError("Cảnh báo", "Vui lòng chọn nhân viên để sửa!");
            return;
        }

        String newUsername = txtUsername.getText();

        // 3. MỚI: Kiểm tra trùng Username (Logic quan trọng khi cho sửa Username)
        // Nếu username mới khác username cũ -> Check xem username mới đã tồn tại chưa
        if (!newUsername.equals(selected.getUsername())) {
            // Giả sử userDAO có hàm check trùng. Nếu chưa có bạn cần viết thêm hoặc dùng try-catch ở tầng DAO
            // Ở đây mình giả định bạn xử lý ở tầng DAO và trả về false nếu lỗi
            // Hoặc quét list hiện tại:
            boolean exists = staffList.stream()
                    .anyMatch(u -> u.getUsername().equals(newUsername) && u.getId() != selected.getId());
            if (exists) {
                AlertUtils.showError("Lỗi", "Username '" + newUsername + "' đã tồn tại! Vui lòng chọn tên khác.");
                return;
            }
        }

        selected.setUsername(newUsername); // Cập nhật username mới
        selected.setFullName(txtFullName.getText());

        // Chỉ cập nhật Role nếu không phải là chính mình (Backend check double protection)
        if (selected.getId() != currentLoggedInUserId) {
            selected.setRole(cbRole.getValue());
        }

        selected.setPhone(txtPhone.getText());
        selected.setAvatarPath(currentAvatarPath);

        if (!txtPassword.getText().isEmpty()) {
            selected.setPassword(txtPassword.getText());
        }

        if (userDAO.updateUser(selected)) {
            AlertUtils.showInfo("Thành công", "Cập nhật thành công!");
            loadData();
            tblStaff.refresh();
        } else {
            AlertUtils.showError("Lỗi", "Cập nhật thất bại (Có thể do lỗi CSDL)!");
        }
    }

    @FXML
    private void handleDelete() {
        User selected = tblStaff.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // MỚI: Chặn xóa chính mình
        if (selected.getId() == currentLoggedInUserId) {
            AlertUtils.showError("Cảnh báo", "Bạn không thể tự xóa tài khoản của chính mình!");
            return;
        }

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
        txtUsername.setDisable(false); // Luôn enable khi clear
        txtPassword.clear();
        txtFullName.clear();
        txtPhone.clear();
        cbRole.getSelectionModel().select("STAFF");
        cbRole.setDisable(false); // Reset lại trạng thái enable cho combo box
        imgAvatar.setImage(null);
        currentAvatarPath = null;
        tblStaff.getSelectionModel().clearSelection();
    }
}