package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.FoodDAO;
import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MenuDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ImageView imgPreview;

    private Food currentFood;
    private String currentImagePath;
    private final FoodDAO foodDAO = new FoodDAO();
    private boolean saveClicked = false; // Để biết người dùng có bấm Lưu không

    public void initialize() {
        cbCategory.setItems(FXCollections.observableArrayList("Món Chính", "Khai Vị", "Đồ Uống", "Tráng Miệng"));
        cbStatus.setItems(FXCollections.observableArrayList("AVAILABLE", "SOLD_OUT", "STOPPED"));
        cbCategory.getSelectionModel().selectFirst();
        cbStatus.getSelectionModel().selectFirst();
    }

    // Hàm nhận dữ liệu từ màn hình chính
    public void setFood(Food food) {
        this.currentFood = food;

        if (food != null) {
            // Chế độ Sửa
            lblTitle.setText("CẬP NHẬT MÓN ĂN");
            txtName.setText(food.getName());
            txtPrice.setText(String.valueOf((int)food.getPrice()));
            cbCategory.setValue(food.getCategory());
            cbStatus.setValue(food.getStatus());
            currentImagePath = food.getImagePath();
            loadImage();
        } else {
            // Chế độ Thêm mới
            lblTitle.setText("THÊM MÓN MỚI");
            currentImagePath = null;
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    private void loadImage() {
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            File file = new File(currentImagePath);
            if (file.exists()) {
                imgPreview.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(txtName.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File destDir = new File("data/images");
                if (!destDir.exists()) destDir.mkdirs();

                String newFileName = "food_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, newFileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                currentImagePath = "data/images/" + newFileName;
                imgPreview.setImage(new Image(destFile.toURI().toString()));
            } catch (IOException e) {
                AlertUtils.showError("Lỗi", "Không thể upload ảnh!");
            }
        }
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtPrice.getText().isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập tên và giá!");
            return;
        }

        try {
            double price = Double.parseDouble(txtPrice.getText());

            if (currentFood == null) {
                // Thêm mới
                Food newFood = new Food(0, txtName.getText(), price, cbCategory.getValue(), cbStatus.getValue(), currentImagePath);
                if (foodDAO.addFood(newFood)) {
                    saveClicked = true;
                    closeDialog();
                } else {
                    AlertUtils.showError("Lỗi", "Thêm thất bại!");
                }
            } else {
                // Cập nhật
                currentFood.setName(txtName.getText());
                currentFood.setPrice(price);
                currentFood.setCategory(cbCategory.getValue());
                currentFood.setStatus(cbStatus.getValue());
                currentFood.setImagePath(currentImagePath);

                if (foodDAO.updateFood(currentFood)) {
                    saveClicked = true;
                    closeDialog();
                } else {
                    AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
                }
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi", "Giá tiền phải là số!");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}