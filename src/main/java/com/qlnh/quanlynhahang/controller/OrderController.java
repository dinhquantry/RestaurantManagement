package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.FoodDAO;
import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.model.OrderItem;
import com.qlnh.quanlynhahang.util.AlertUtils;
import com.qlnh.quanlynhahang.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderController {

    // Bảng Menu sử dụng Model Food chuẩn
    @FXML private TableView<Food> tblMenu;
    @FXML private TableColumn<Food, String> colFoodName;
    @FXML private TableColumn<Food, Double> colPrice;
    @FXML private TableColumn<Food, String> colStatus;

    // Bảng Order sử dụng Model OrderItem mới
    @FXML private TableView<OrderItem> tblOrder;
    @FXML private TableColumn<OrderItem, String> colOrderName;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colTotal;

    @FXML private Label lblTotalAmount;

    private int tableId;
    private final FoodDAO foodDAO = new FoodDAO(); // Dùng DAO có sẵn
    private ObservableList<OrderItem> currentOrderList = FXCollections.observableArrayList();

    public void setTableId(int id) {
        this.tableId = id;
        loadMenu();
    }

    public void initialize() {
        // Cấu hình cột bảng Menu (khớp với Model Food)
        colFoodName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cấu hình cột bảng Order (khớp với Model OrderItem)
        colOrderName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        tblOrder.setItems(currentOrderList);

        // Load menu ngay (để test nếu chạy độc lập)
        loadMenu();
    }

    private void loadMenu() {
        // Sử dụng FoodDAO để lấy dữ liệu, vừa gọn vừa tái sử dụng code
        List<Food> list = foodDAO.getAllFoods();
        ObservableList<Food> menuData = FXCollections.observableArrayList(list);
        tblMenu.setItems(menuData);
    }

    @FXML
    private void handleAddToOrder() {
        Food selected = tblMenu.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if ("SOLD_OUT".equals(selected.getStatus()) || "STOPPED".equals(selected.getStatus())) {
            AlertUtils.showError("Hết món", "Món này hiện không phục vụ!");
            return;
        }

        boolean exists = false;
        for (OrderItem item : currentOrderList) {
            if (item.getFoodId() == selected.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                item.setTotalPrice(item.getQuantity() * selected.getPrice());
                exists = true;
                tblOrder.refresh();
                break;
            }
        }

        if (!exists) {
            currentOrderList.add(new OrderItem(
                    selected.getId(),
                    selected.getName(),
                    1,
                    selected.getPrice()
            ));
        }

        updateTotalAmount();
    }

    private void updateTotalAmount() {
        double total = currentOrderList.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        lblTotalAmount.setText(String.format("Tổng tiền: %,.0f VND", total));
    }

    @FXML
    private void handleSaveOrder() {
        if (currentOrderList.isEmpty()) {
            AlertUtils.showError("Lỗi", "Chưa chọn món nào!");
            return;
        }

        // Cập nhật trạng thái bàn -> Có khách
        updateTableStatus(this.tableId, "OCCUPIED");

        // Giai đoạn này bạn có thể gọi OrderDAO để lưu hóa đơn thật vào DB
        // ... orderDAO.createOrder(...)

        AlertUtils.showInfo("Thành công", "Đã gửi order xuống bếp!");

        // Xóa list sau khi gửi (hoặc đóng cửa sổ)
        // currentOrderList.clear();
        // updateTotalAmount();
    }

    private void updateTableStatus(int id, String status) {
        String sql = "UPDATE Tables SET status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handlePayment(javafx.event.ActionEvent event) {
        if (currentOrderList.isEmpty()) {
            AlertUtils.showError("Lỗi", "Bàn chưa có món nào để thanh toán!");
            return;
        }

        double total = currentOrderList.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        String totalStr = String.format("%,.0f VND", total);

        // Hiển thị xác nhận
        if (AlertUtils.showConfirmation("Xác nhận thanh toán",
                "Bạn có chắc muốn thanh toán cho bàn này?\nTổng tiền: " + totalStr)) {

            // 1. Lưu hóa đơn vào DB (Giả lập gọi OrderDAO)
            // Order newOrder = new Order(tableId, userId, total, "PAID");
            // orderDAO.createOrder(newOrder);
            // Lưu ý: Để code ngắn gọn mình đang bỏ qua bước lưu chi tiết OrderDAO ở đây,
            // thực tế bạn cần gọi orderDAO.createOrder() và orderDetailDAO.add()

            // 2. Cập nhật trạng thái bàn về TRỐNG (EMPTY)
            updateTableStatus(this.tableId, "EMPTY");

            AlertUtils.showInfo("Thành công", "Đã thanh toán xong! Bàn đã trống.");

            // 3. Đóng cửa sổ Gọi món
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();
        }
    }
}