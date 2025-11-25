package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.TableDAO;
import com.qlnh.quanlynhahang.model.DiningTable;
import com.qlnh.quanlynhahang.model.User; // Import User
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class TableMapController {

    @FXML private FlowPane tableContainer;
    @FXML private ComboBox<String> cbFloor;
    @FXML private Button btnAddTable;

    private final TableDAO tableDAO = new TableDAO();
    private List<DiningTable> allTables;
    private User currentUser; // Biến lưu người dùng hiện tại

    public void initialize() {
        cbFloor.setItems(FXCollections.observableArrayList("Tất cả", "Tầng 1", "Tầng 2", "Tầng 3"));
        cbFloor.getSelectionModel().selectFirst();

        loadTables();
    }

    // Hàm nhận User từ Dashboard truyền sang
    public void initData(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleFilterFloor() {
        renderTables();
    }

    private void loadTables() {
        allTables = tableDAO.getAllTables();
        renderTables();
    }

    private void renderTables() {
        tableContainer.getChildren().clear();
        String selectedFloorStr = cbFloor.getValue();
        int selectedFloor = -1;

        if (selectedFloorStr != null && !selectedFloorStr.equals("Tất cả")) {
            selectedFloor = Integer.parseInt(selectedFloorStr.replace("Tầng ", ""));
        }

        for (DiningTable t : allTables) {
            if (selectedFloor != -1 && t.getFloor() != selectedFloor) {
                continue;
            }
            Button btn = createTableButton(t);
            tableContainer.getChildren().add(btn);
        }
    }

    private Button createTableButton(DiningTable t) {
        Button btn = new Button(t.getName() + "\n(" + t.getCapacity() + " ghế)");
        btn.setPrefSize(120, 120);
        btn.setWrapText(true);

        String style = "-fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 10; ";
        switch (t.getStatus()) {
            case "EMPTY": style += "-fx-background-color: #27ae60;"; break; // Xanh
            case "OCCUPIED": style += "-fx-background-color: #c0392b;"; break; // Đỏ
            case "BOOKED": style += "-fx-background-color: #f39c12;"; break; // Vàng
        }
        btn.setStyle(style);

        // Menu chuột phải (Admin/Quản lý dùng)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Sửa thông tin");
        MenuItem deleteItem = new MenuItem("Xóa bàn");

        editItem.setOnAction(e -> showEditDialog(t));
        deleteItem.setOnAction(e -> handleDeleteTable(t));

        contextMenu.getItems().addAll(editItem, deleteItem);
        btn.setContextMenu(contextMenu);

        // Chuột trái: Hiện hộp thoại lựa chọn Gọi món / Đặt bàn
        btn.setOnAction(e -> handleTableClick(t));

        return btn;
    }

    // --- XỬ LÝ CLICK BÀN (MỚI) ---
    private void handleTableClick(DiningTable t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thao tác: " + t.getName());
        alert.setHeaderText("Trạng thái hiện tại: " + translateStatus(t.getStatus()));
        alert.setContentText("Bạn muốn thực hiện thao tác nào?");

        ButtonType btnOrder = new ButtonType("Gọi món");
        ButtonType btnBooking = new ButtonType("Đặt bàn");
        ButtonType btnCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        // Logic hiển thị nút
        if ("EMPTY".equals(t.getStatus())) {
            // Bàn trống: Được Gọi món hoặc Đặt bàn
            alert.getButtonTypes().setAll(btnOrder, btnBooking, btnCancel);
        } else {
            // Bàn Có khách/Đã đặt: Chỉ được Gọi món (để thêm món hoặc xem bill)
            alert.getButtonTypes().setAll(btnOrder, btnCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnOrder) {
                openOrderScreen(t);
            } else if (result.get() == btnBooking) {
                showBookingDialog(t);
            }
        }
    }

    // Mở màn hình gọi món
    private void openOrderScreen(DiningTable t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderView.fxml"));
            Parent root = loader.load();

            OrderController controller = loader.getController();
            controller.setTableId(t.getId());
            // QUAN TRỌNG: Truyền User sang OrderController để lưu đúng ID người bán
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Gọi món - " + t.getName());
            stage.showAndWait();

            loadTables(); // Reload sau khi đóng màn hình order (để cập nhật màu sắc)
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở màn hình gọi món!");
        }
    }

    // Xử lý Đặt bàn (Đổi trạng thái sang Vàng)
    private void showBookingDialog(DiningTable t) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt bàn - " + t.getName());
        dialog.setHeaderText("Nhập thông tin khách đặt (Tên - SĐT - Giờ)");
        dialog.setContentText("Ghi chú:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(info -> {
            // Cập nhật trạng thái thành BOOKED
            if (tableDAO.updateStatus(t.getId(), "BOOKED")) {
                // (Nâng cao: Bạn có thể gọi BookingDAO để lưu thông tin khách vào bảng Bookings tại đây)
                AlertUtils.showInfo("Thành công", "Đã đặt bàn cho: " + info);
                loadTables();
            } else {
                AlertUtils.showError("Lỗi", "Không thể cập nhật trạng thái bàn!");
            }
        });
    }

    private String translateStatus(String status) {
        switch (status) {
            case "EMPTY": return "Bàn Trống";
            case "OCCUPIED": return "Đang có khách";
            case "BOOKED": return "Đã đặt trước";
            default: return status;
        }
    }

    // --- CÁC HÀM QUẢN LÝ BÀN (ADD/EDIT/DELETE) GIỮ NGUYÊN ---
    @FXML
    private void handleAddTable() {
        showTableDialog(null);
    }

    private void showEditDialog(DiningTable t) {
        showTableDialog(t);
    }

    private void showTableDialog(DiningTable tableToEdit) {
        Dialog<DiningTable> dialog = new Dialog<>();
        dialog.setTitle(tableToEdit == null ? "Thêm Bàn Mới" : "Sửa Bàn " + tableToEdit.getName());
        dialog.setHeaderText("Nhập thông tin bàn:");

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Tên bàn");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Số ghế");
        ComboBox<Integer> floorCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
        floorCombo.getSelectionModel().selectFirst();

        if (tableToEdit != null) {
            nameField.setText(tableToEdit.getName());
            capacityField.setText(String.valueOf(tableToEdit.getCapacity()));
            floorCombo.getSelectionModel().select(Integer.valueOf(tableToEdit.getFloor()));
        }

        grid.add(new Label("Tên bàn:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Số ghế:"), 0, 1);
        grid.add(capacityField, 1, 1);
        grid.add(new Label("Tầng:"), 0, 2);
        grid.add(floorCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                DiningTable t = (tableToEdit != null) ? tableToEdit : new DiningTable();
                t.setName(nameField.getText());
                try {
                    t.setCapacity(Integer.parseInt(capacityField.getText()));
                } catch (Exception e) { t.setCapacity(4); }
                t.setFloor(floorCombo.getValue());
                return t;
            }
            return null;
        });

        Optional<DiningTable> result = dialog.showAndWait();
        result.ifPresent(t -> {
            if (tableToEdit == null) {
                if (tableDAO.addTable(t)) {
                    AlertUtils.showInfo("Thành công", "Đã thêm bàn mới!");
                    loadTables();
                }
            } else {
                if (tableDAO.updateTableInfo(t)) {
                    AlertUtils.showInfo("Thành công", "Đã cập nhật thông tin bàn!");
                    loadTables();
                }
            }
        });
    }

    private void handleDeleteTable(DiningTable t) {
        if (!"EMPTY".equals(t.getStatus())) {
            AlertUtils.showError("Lỗi", "Không thể xóa bàn đang có khách hoặc đã đặt!");
            return;
        }
        if (AlertUtils.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa " + t.getName() + "?")) {
            if (tableDAO.deleteTable(t.getId())) {
                AlertUtils.showInfo("Thành công", "Đã xóa bàn!");
                loadTables();
            }
        }
    }
}