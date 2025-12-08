package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.BookingDAO;
import com.qlnh.quanlynhahang.dao.TableDAO;
import com.qlnh.quanlynhahang.model.Booking;
import com.qlnh.quanlynhahang.model.DiningTable;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TableMapController {

    @FXML private FlowPane tableContainer;
    @FXML private ComboBox<String> cbFloor;
    @FXML private Button btnAddTable;

    private final TableDAO tableDAO = new TableDAO();
    private final BookingDAO bookingDAO = new BookingDAO(); // Thêm DAO để lấy thông tin đặt bàn
    private List<DiningTable> allTables;
    private User currentUser;

    public void initialize() {
        cbFloor.setItems(FXCollections.observableArrayList("Tất cả", "Tầng 1", "Tầng 2", "Tầng 3"));
        cbFloor.getSelectionModel().selectFirst();

        loadTables();
    }

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
            case "EMPTY": style += "-fx-background-color: #27ae60;"; break;
            case "OCCUPIED": style += "-fx-background-color: #c0392b;"; break;
            case "BOOKED": style += "-fx-background-color: #f39c12;"; break;
        }
        btn.setStyle(style);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Sửa thông tin");
        MenuItem deleteItem = new MenuItem("Xóa bàn");

        editItem.setOnAction(e -> showTableDialog(t));
        deleteItem.setOnAction(e -> handleDeleteTable(t));

        contextMenu.getItems().addAll(editItem, deleteItem);
        btn.setContextMenu(contextMenu);

        btn.setOnAction(e -> handleTableClick(t));

        return btn;
    }

    // --- XỬ LÝ CLICK BÀN ---
    private void handleTableClick(DiningTable t) {
        if ("OCCUPIED".equals(t.getStatus())) {
            openOrderScreen(t);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thao tác: " + t.getName());
        alert.setHeaderText("Trạng thái hiện tại: " + translateStatus(t.getStatus()));
        alert.setContentText("Bạn muốn thực hiện thao tác nào?");

        ButtonType btnOrder = new ButtonType("Gọi món (Nhận khách)");
        ButtonType btnBooking = new ButtonType("Đặt bàn");
        ButtonType btnViewInfo = new ButtonType("Xem thông tin"); // Nút xem thông tin khách
        ButtonType btnCancelBooking = new ButtonType("Hủy đặt bàn");
        ButtonType btnCancel = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);

        if ("EMPTY".equals(t.getStatus())) {
            alert.getButtonTypes().setAll(btnOrder, btnBooking, btnCancel);
        } else if ("BOOKED".equals(t.getStatus())) {
            // Thêm nút Xem thông tin cho bàn đã đặt
            alert.getButtonTypes().setAll(btnOrder, btnViewInfo, btnCancelBooking, btnCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnOrder) {
                openOrderScreen(t);
            } else if (result.get() == btnBooking) {
                showBookingDialog(t);
            } else if (result.get() == btnViewInfo) {
                showBookingInfo(t);
            } else if (result.get() == btnCancelBooking) {
                if (tableDAO.updateStatus(t.getId(), "EMPTY")) {
                    AlertUtils.showInfo("Thành công", "Đã hủy đặt bàn " + t.getName());
                    loadTables();
                } else {
                    AlertUtils.showError("Lỗi", "Không thể hủy đặt bàn!");
                }
            }
        }
    }

    // --- HIỂN THỊ THÔNG TIN ĐẶT BÀN ---
    private void showBookingInfo(DiningTable t) {
        // Giả sử BookingDAO có hàm getLatestBooking lấy thông tin đặt bàn gần nhất/sắp tới của bàn này
        Booking booking = bookingDAO.getLatestBooking(t.getId());

        if (booking != null) {
            String info = "Khách hàng: " + booking.getCustomerName() + "\n" +
                    "Số điện thoại: " + booking.getPhone() + "\n" +
                    "Số lượng khách: " + booking.getGuestCount() + "\n" +
                    "Thời gian: " + booking.getBookingTime();

            AlertUtils.showInfo("Thông tin đặt bàn - " + t.getName(), info);
        } else {
            AlertUtils.showError("Lỗi", "Không tìm thấy thông tin đặt bàn!");
        }
    }

    // --- MỞ CÁC MÀN HÌNH ---

    private void openOrderScreen(DiningTable t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderView.fxml"));
            Parent root = loader.load();

            OrderController controller = loader.getController();
            controller.setTableId(t.getId());
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Gọi món - " + t.getName());
            stage.showAndWait();

            loadTables();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở màn hình gọi món!");
        }
    }

    // --- QUẢN LÝ BÀN (THÊM / SỬA) ---
    @FXML
    private void handleAddTable() {
        showTableDialog(null);
    }

    private void showEditDialog(DiningTable t) {
        showTableDialog(t);
    }

    private void showTableDialog(DiningTable tableToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TableDialog.fxml"));
            Parent root = loader.load();

            TableDialogController controller = loader.getController();
            controller.setTable(tableToEdit);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(tableToEdit == null ? "Thêm Bàn Mới" : "Sửa Bàn");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaveClicked()) {
                loadTables();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở hộp thoại quản lý bàn!");
        }
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

    // --- FORM ĐẶT BÀN (BOOKING) - ĐÃ TÁCH RA ---
    private void showBookingDialog(DiningTable t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookingDialog.fxml"));
            Parent root = loader.load();

            BookingDialogController controller = loader.getController();
            controller.setTable(t);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Đặt bàn - " + t.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaveClicked()) {
                loadTables();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở hộp thoại đặt bàn!");
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "EMPTY": return "Bàn Trống";
            case "OCCUPIED": return "Đang có khách";
            case "BOOKED": return "Đã đặt trước";
            default: return status;
        }
    }
}