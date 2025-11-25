package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.util.AlertUtils;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class TableMapController {

    @FXML private FlowPane tableContainer; // Container chứa các nút bàn
    @FXML private Label lblStatus;

    public void initialize() {
        loadTables();
    }

    // US2.2: Lấy danh sách và trạng thái bàn
    private void loadTables() {
        tableContainer.getChildren().clear();
        String query = "SELECT * FROM Tables";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("table_id");
                String name = rs.getString("table_name");
                String status = rs.getString("status");

                Button btn = createTableButton(id, name, status);
                tableContainer.getChildren().add(btn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Button createTableButton(int id, String name, String status) {
        Button btn = new Button(name + "\n" + status);
        btn.setPrefSize(100, 100);

        // Màu sắc theo trạng thái
        String style = "-fx-font-weight: bold; -fx-text-fill: white; ";
        switch (status) {
            case "EMPTY": style += "-fx-background-color: #27ae60;"; break; // Xanh
            case "OCCUPIED": style += "-fx-background-color: #c0392b;"; break; // Đỏ
            case "BOOKED": style += "-fx-background-color: #f39c12;"; break; // Vàng
        }
        btn.setStyle(style);

        // Sự kiện click vào bàn
        btn.setOnAction(e -> handleTableClick(id, name, status));
        return btn;
    }

    private void handleTableClick(int tableId, String tableName, String currentStatus) {
        // Nếu bàn trống -> Mở dialog đặt bàn hoặc gọi món
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thao tác với " + tableName);
        alert.setHeaderText("Trạng thái hiện tại: " + currentStatus);
        alert.setContentText("Bạn muốn làm gì?");

        ButtonType btnOrder = new ButtonType("Gọi món (Order)");
        ButtonType btnBook = new ButtonType("Đặt bàn");
        ButtonType btnCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnOrder, btnBook, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == btnOrder) {
            openOrderScreen(tableId); // Chuyển sang US3.1
        } else if (result.get() == btnBook) {
            showBookingDialog(tableId); // US2.1
        }
    }

    // US2.1: Nhập thông tin đặt bàn
    private void showBookingDialog(int tableId) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt bàn");
        dialog.setHeaderText("Nhập tên khách hàng & SĐT");
        dialog.setContentText("Tên - SĐT:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(info -> {
            updateTableStatus(tableId, "BOOKED");
            // Thực tế cần Insert vào bảng Bookings ở đây
            AlertUtils.showInfo("Thành công", "Đã đặt bàn cho: " + info);
            loadTables(); // Reload lại giao diện
        });
    }

    private void updateTableStatus(int tableId, String newStatus) {
        String sql = "UPDATE Tables SET status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, tableId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openOrderScreen(int tableId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderView.fxml"));
            Parent root = loader.load();

            OrderController controller = loader.getController();
            controller.setTableId(tableId); // Truyền ID bàn sang màn hình Order

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Gọi món - Bàn " + tableId);
            stage.showAndWait();

            loadTables(); // Reload sau khi đóng màn hình order (để cập nhật trạng thái đỏ)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}