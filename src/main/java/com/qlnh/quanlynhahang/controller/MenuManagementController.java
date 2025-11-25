package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.FoodDAO;
import com.qlnh.quanlynhahang.model.Food;
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

public class MenuManagementController {

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ImageView imgPreview;

    @FXML private TableView<Food> tblFoods;
    @FXML private TableColumn<Food, Integer> colId;
    @FXML private TableColumn<Food, String> colName;
    @FXML private TableColumn<Food, Double> colPrice;
    @FXML private TableColumn<Food, String> colCategory;
    @FXML private TableColumn<Food, String> colStatus;

    private final FoodDAO foodDAO = new FoodDAO();
    private ObservableList<Food> foodList = FXCollections.observableArrayList();
    private String currentImagePath = null; // Lưu đường dẫn ảnh tạm thời

    public void initialize() {
        // Init ComboBox
        cbCategory.setItems(FXCollections.observableArrayList("Món Chính", "Khai Vị", "Đồ Uống", "Tráng Miệng"));
        cbStatus.setItems(FXCollections.observableArrayList("AVAILABLE", "SOLD_OUT", "STOPPED"));
        cbCategory.getSelectionModel().selectFirst();
        cbStatus.getSelectionModel().selectFirst();

        // Init Table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadData();

        // Sự kiện khi chọn 1 dòng trên bảng -> Đổ dữ liệu lên form
        tblFoods.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });
    }

    private void loadData() {
        List<Food> list = foodDAO.getAllFoods();
        foodList.setAll(list);
        tblFoods.setItems(foodList);
    }

    private void fillForm(Food f) {
        txtName.setText(f.getName());
        txtPrice.setText(String.valueOf((int)f.getPrice()));
        cbCategory.setValue(f.getCategory());
        cbStatus.setValue(f.getStatus());
        currentImagePath = f.getImagePath();

        // Load ảnh preview
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            File file = new File(currentImagePath);
            if (file.exists()) {
                imgPreview.setImage(new Image(file.toURI().toString()));
            } else {
                imgPreview.setImage(null);
            }
        } else {
            imgPreview.setImage(null);
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh món ăn");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(txtName.getScene().getWindow());

        if (selectedFile != null) {
            // Logic copy file vào thư mục dự án (data/images)
            try {
                File destDir = new File("data/images");
                if (!destDir.exists()) destDir.mkdirs(); // Tạo thư mục nếu chưa có

                String newFileName = "food_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Lưu đường dẫn tương đối để lưu vào DB
                currentImagePath = "data/images/" + newFileName;

                // Hiển thị lên ImageView
                imgPreview.setImage(new Image(destFile.toURI().toString()));

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Lỗi", "Không thể upload ảnh!");
            }
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInput()) return;

        Food f = new Food();
        f.setName(txtName.getText());
        f.setPrice(Double.parseDouble(txtPrice.getText()));
        f.setCategory(cbCategory.getValue());
        f.setStatus(cbStatus.getValue());
        f.setImagePath(currentImagePath);

        if (foodDAO.addFood(f)) {
            AlertUtils.showInfo("Thành công", "Đã thêm món mới!");
            loadData();
            handleClear();
        } else {
            AlertUtils.showError("Lỗi", "Thêm thất bại!");
        }
    }

    @FXML
    private void handleUpdate() {
        Food selected = tblFoods.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Lỗi", "Vui lòng chọn món cần sửa!");
            return;
        }
        if (!validateInput()) return;

        selected.setName(txtName.getText());
        selected.setPrice(Double.parseDouble(txtPrice.getText()));
        selected.setCategory(cbCategory.getValue());
        selected.setStatus(cbStatus.getValue());
        selected.setImagePath(currentImagePath);

        if (foodDAO.updateFood(selected)) {
            AlertUtils.showInfo("Thành công", "Cập nhật thành công!");
            loadData();
            tblFoods.refresh();
        } else {
            AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
        }
    }

    @FXML
    private void handleDelete() {
        Food selected = tblFoods.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (AlertUtils.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa món: " + selected.getName() + "?")) {
            if (foodDAO.deleteFood(selected.getId())) {
                AlertUtils.showInfo("Thành công", "Đã xóa!");
                loadData();
                handleClear();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtName.clear();
        txtPrice.clear();
        cbCategory.getSelectionModel().selectFirst();
        cbStatus.getSelectionModel().selectFirst();
        imgPreview.setImage(null);
        currentImagePath = null;
        tblFoods.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (txtName.getText().isEmpty() || txtPrice.getText().isEmpty()) {
            AlertUtils.showError("Thiếu thông tin", "Vui lòng nhập tên và giá món!");
            return false;
        }
        try {
            Double.parseDouble(txtPrice.getText());
        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi định dạng", "Giá tiền phải là số!");
            return false;
        }
        return true;
    }
}