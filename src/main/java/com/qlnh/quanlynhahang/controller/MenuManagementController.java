package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.FoodDAO;
import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class MenuManagementController {

    @FXML private TableView<Food> tblFoods;
    @FXML private TableColumn<Food, Integer> colSTT;
    @FXML private TableColumn<Food, String> colName;
    @FXML private TableColumn<Food, Double> colPrice;
    @FXML private TableColumn<Food, String> colCategory;
    @FXML private TableColumn<Food, String> colStatus;
    @FXML private TableColumn<Food, Void> colAction; // Cột hành động

    private final FoodDAO foodDAO = new FoodDAO();
    private ObservableList<Food> foodList = FXCollections.observableArrayList();

    public void initialize() {
        // 1. Cấu hình Cột STT (Số thứ tự tự tăng)
        colSTT.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tblFoods.getItems().indexOf(column.getValue()) + 1));

        // 2. Các cột dữ liệu
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 3. Cấu hình Cột Hành Động (Chứa 2 nút Sửa/Xóa)
        setupActionColumn();

        loadData();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Food, Void>, TableCell<Food, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Food, Void> call(final TableColumn<Food, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Sửa");
                    private final Button btnDelete = new Button("Xóa");
                    private final HBox pane = new HBox(10, btnEdit, btnDelete);

                    {
                        // Style nút Sửa
                        btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                        btnEdit.setOnAction(event -> {
                            Food food = getTableView().getItems().get(getIndex());
                            showDialog(food); // Mở Popup Sửa
                        });

                        // Style nút Xóa
                        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> {
                            Food food = getTableView().getItems().get(getIndex());
                            handleDelete(food);
                        });

                        pane.setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    private void loadData() {
        List<Food> list = foodDAO.getAllFoods();
        foodList.setAll(list);
        tblFoods.setItems(foodList);
    }

    @FXML
    private void handleAddNew() {
        showDialog(null); // Null = Chế độ Thêm mới
    }

    private void showDialog(Food food) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuDialog.fxml"));
            Parent root = loader.load();

            // Truyền dữ liệu vào Dialog
            MenuDialogController controller = loader.getController();
            controller.setFood(food);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ cha
            stage.setTitle(food == null ? "Thêm Món Mới" : "Cập Nhật Món Ăn");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Chờ đóng

            // Nếu người dùng đã bấm Lưu thì reload lại bảng
            if (controller.isSaveClicked()) {
                loadData();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở hộp thoại!");
        }
    }

    private void handleDelete(Food food) {
        if (AlertUtils.showConfirmation("Xác nhận xóa", "Bạn có chắc muốn xóa món: " + food.getName() + "?")) {
            if (foodDAO.deleteFood(food.getId())) {
                AlertUtils.showInfo("Thành công", "Đã xóa món ăn!");
                loadData();
            }
        }
    }
}