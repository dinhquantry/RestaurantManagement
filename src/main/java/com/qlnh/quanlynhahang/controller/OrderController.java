package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.FoodDAO;
import com.qlnh.quanlynhahang.dao.OrderDAO;
import com.qlnh.quanlynhahang.dao.OrderDetailDAO;
import com.qlnh.quanlynhahang.dao.TableDAO;
import com.qlnh.quanlynhahang.model.Food;
import com.qlnh.quanlynhahang.model.Order;
import com.qlnh.quanlynhahang.model.OrderDetail;
import com.qlnh.quanlynhahang.model.OrderItem;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Import này quan trọng
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;    // Import Parent
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;  // Import Modality
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;    // Import IOException
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderController {

    @FXML private TableView<Food> tblMenu;
    @FXML private TableColumn<Food, String> colFoodName;
    @FXML private TableColumn<Food, Double> colPrice;
    @FXML private TableColumn<Food, String> colStatus;

    @FXML private TableView<OrderItem> tblOrder;
    @FXML private TableColumn<OrderItem, String> colOrderName;
    @FXML private TableColumn<OrderItem, Integer> colQuantity;
    @FXML private TableColumn<OrderItem, Double> colTotal;
    @FXML private TableColumn<OrderItem, Void> colAction;

    @FXML private Label lblTotalAmount;
    @FXML private Spinner<Integer> spnQuantity;

    private int tableId;
    private int currentOrderId = -1;
    private User currentUser;

    private final FoodDAO foodDAO = new FoodDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderDetailDAO detailDAO = new OrderDetailDAO();
    private final TableDAO tableDAO = new TableDAO();

    private ObservableList<OrderItem> currentOrderList = FXCollections.observableArrayList();

    public void initData(User user) {
        this.currentUser = user;
    }

    public void setTableId(int id) {
        this.tableId = id;

        this.currentOrderId = orderDAO.getPendingOrderId(tableId);

        if (this.currentOrderId != -1) {
            List<OrderItem> oldItems = detailDAO.getOrderItems(this.currentOrderId);
            currentOrderList.setAll(oldItems);
            updateTotalAmount();
        }
    }

    public void initialize() {
        colFoodName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colOrderName.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        setupActionColumn();

        tblOrder.setItems(currentOrderList);

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spnQuantity.setValueFactory(valueFactory);

        loadMenu();
    }

    private void setupActionColumn() {
        Callback<TableColumn<OrderItem, Void>, TableCell<OrderItem, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<OrderItem, Void> call(final TableColumn<OrderItem, Void> param) {
                return new TableCell<>() {
                    private final Button btnDec = new Button("-");
                    private final Button btnInc = new Button("+");
                    private final Button btnDel = new Button("X");
                    private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5, btnDec, btnInc, btnDel);

                    {
                        btnDec.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 25px; -fx-cursor: hand;");
                        btnDec.setOnAction(event -> {
                            OrderItem item = getTableView().getItems().get(getIndex());
                            changeQuantity(item, -1);
                        });

                        btnInc.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 25px; -fx-cursor: hand;");
                        btnInc.setOnAction(event -> {
                            OrderItem item = getTableView().getItems().get(getIndex());
                            changeQuantity(item, 1);
                        });

                        btnDel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 25px; -fx-cursor: hand;");
                        btnDel.setOnAction(event -> {
                            OrderItem item = getTableView().getItems().get(getIndex());
                            currentOrderList.remove(item);
                            updateTotalAmount();
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

    private void changeQuantity(OrderItem item, int delta) {
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            currentOrderList.remove(item);
        } else {
            item.setQuantity(newQty);
            item.setTotalPrice(newQty * item.getUnitPrice());
            tblOrder.refresh();
        }
        updateTotalAmount();
    }

    private void loadMenu() {
        List<Food> list = foodDAO.getAllFoods();
        tblMenu.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    private void handleAddToOrder() {
        Food selected = tblMenu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Lỗi", "Vui lòng chọn món ăn!");
            return;
        }

        if ("SOLD_OUT".equals(selected.getStatus()) || "STOPPED".equals(selected.getStatus())) {
            AlertUtils.showError("Hết món", "Món này hiện không phục vụ!");
            return;
        }

        int quantityToAdd = spnQuantity.getValue();

        boolean exists = false;
        for (OrderItem item : currentOrderList) {
            if (item.getFoodId() == selected.getId()) {
                int newQty = item.getQuantity() + quantityToAdd;
                item.setQuantity(newQty);
                item.setTotalPrice(newQty * item.getUnitPrice());
                exists = true;
                tblOrder.refresh();
                break;
            }
        }

        if (!exists) {
            currentOrderList.add(new OrderItem(
                    selected.getId(), selected.getName(), quantityToAdd, selected.getPrice(), selected.getPrice() * quantityToAdd
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
        if (this.tableId == 0) {
            AlertUtils.showError("Lưu ý", "Vui lòng chọn bàn từ Sơ đồ bàn để gọi món!");
            return;
        }

        if (currentOrderList.isEmpty()) {
            AlertUtils.showError("Lỗi", "Chưa chọn món nào!");
            return;
        }

        if (this.currentOrderId == -1) {
            Order newOrder = new Order();
            newOrder.setTableId(this.tableId);
            newOrder.setUserId(currentUser != null ? currentUser.getId() : 1);

            this.currentOrderId = orderDAO.createOrder(newOrder);
        }

        if (this.currentOrderId != -1) {
            detailDAO.clearDetails(this.currentOrderId);

            for (OrderItem item : currentOrderList) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderId(this.currentOrderId);
                detail.setFoodId(item.getFoodId());
                detail.setQuantity(item.getQuantity());
                detail.setPriceAtOrder(item.getUnitPrice());

                detailDAO.addOrderDetail(detail);
            }

            tableDAO.updateStatus(this.tableId, "OCCUPIED");

            double total = currentOrderList.stream().mapToDouble(OrderItem::getTotalPrice).sum();
            orderDAO.updateTotalAmount(this.currentOrderId, total);

            AlertUtils.showInfo("Thành công", "Đã lưu đơn hàng xuống Bếp!");
        }
    }

    @FXML
    private void handlePayment(javafx.event.ActionEvent event) {
        if (this.tableId == 0) {
            AlertUtils.showError("Lỗi", "Chức năng này chỉ dùng khi đã chọn bàn!");
            return;
        }
        if (currentOrderList.isEmpty()) return;

        double total = currentOrderList.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        if (AlertUtils.showConfirmation("Thanh toán", "Tổng tiền: " + String.format("%,.0f", total) + " VND. Xác nhận thanh toán?")) {

            if (this.currentOrderId != -1) {
                orderDAO.payOrder(this.currentOrderId);
            }

            tableDAO.updateStatus(this.tableId, "EMPTY");

            AlertUtils.showInfo("Hoàn tất", "Đã thanh toán và trả bàn.");

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handlePrintInvoice(javafx.event.ActionEvent event) {
        if (currentOrderList.isEmpty()) {
            AlertUtils.showError("Lỗi", "Không có món nào để xuất hóa đơn!");
            return;
        }

        // Tạo nội dung HTML
        String htmlContent = generateInvoiceHtmlString();

        // Mở cửa sổ xem trước (Dùng FXML tách biệt)
        showPreviewWindow(htmlContent);
    }

    // Hàm mở cửa sổ xem trước đã được tách logic
    private void showPreviewWindow(String htmlContent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InvoicePreview.fxml"));
            Parent root = loader.load();

            // Lấy Controller của màn hình xem trước và truyền HTML vào
            InvoicePreviewController controller = loader.getController();
            controller.setInvoiceContent(htmlContent);

            Stage stage = new Stage();
            stage.setTitle("Xem trước Hóa Đơn - Bàn " + tableId);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Chặn cửa sổ cha
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể mở cửa sổ xem trước!");
        }
    }

    private String generateInvoiceHtmlString() {
        double total = currentOrderList.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: 'Segoe UI', sans-serif; padding: 20px; background-color: #fff; }");
        html.append(".header { text-align: center; margin-bottom: 20px; border-bottom: 2px dashed #000; padding-bottom: 10px; }");
        html.append("h1 { margin: 0; font-size: 24px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th { border-bottom: 1px solid #000; text-align: left; padding: 5px; }");
        html.append("td { border-bottom: 1px dotted #ccc; padding: 5px; }");
        html.append(".amount { text-align: right; }");
        html.append(".total { font-size: 1.5em; font-weight: bold; text-align: right; margin-top: 20px; }");
        html.append(".footer { text-align: center; margin-top: 30px; font-style: italic; font-size: 0.9em; }");
        html.append("</style></head><body>");

        html.append("<div class='header'>");
        html.append("<h1>NHÀ HÀNG CỦA BẠN</h1>");
        html.append("<p>123 Đường Ẩm Thực, TP.HCM</p>");
        html.append("<p>Bàn: ").append(tableId == 0 ? "Mang về" : tableId).append(" | Đơn: #").append(currentOrderId == -1 ? "MỚI" : currentOrderId).append("</p>");
        html.append("<p>Thời gian: ").append(date).append("</p>");
        html.append("</div>");

        html.append("<table>");
        html.append("<thead><tr><th>Tên món</th><th style='text-align: center;'>SL</th><th class='amount'>Thành tiền</th></tr></thead>");
        html.append("<tbody>");
        for (OrderItem item : currentOrderList) {
            html.append("<tr>");
            html.append("<td>").append(item.getFoodName()).append("</td>");
            html.append("<td style='text-align: center;'>").append(item.getQuantity()).append("</td>");
            html.append("<td class='amount'>").append(String.format("%,.0f", item.getTotalPrice())).append("</td>");
            html.append("</tr>");
        }
        html.append("</tbody></table>");

        html.append("<div class='total'>TỔNG: ").append(String.format("%,.0f VND", total)).append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Cảm ơn quý khách!</p>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}