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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
        loadMenu();

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

        tblOrder.setItems(currentOrderList);

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        spnQuantity.setValueFactory(valueFactory);
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

    @FXML
    private void handleIncrease() {
        OrderItem selected = tblOrder.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        int newQty = selected.getQuantity() + 1;
        double unitPrice = selected.getUnitPrice();

        selected.setQuantity(newQty);
        selected.setTotalPrice(newQty * unitPrice);

        tblOrder.refresh();
        updateTotalAmount();
    }

    @FXML
    private void handleDecrease() {
        OrderItem selected = tblOrder.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.getQuantity() > 1) {
            int newQty = selected.getQuantity() - 1;
            double unitPrice = selected.getUnitPrice();

            selected.setQuantity(newQty);
            selected.setTotalPrice(newQty * unitPrice);
            tblOrder.refresh();
            updateTotalAmount();
        } else {
            handleDeleteItem();
        }
    }

    @FXML
    private void handleDeleteItem() {
        OrderItem selected = tblOrder.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Lỗi", "Vui lòng chọn món cần xóa!");
            return;
        }

        currentOrderList.remove(selected);
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

    // --- CHỈ CÒN CHỨC NĂNG IN / LƯU PDF ---
    @FXML
    private void handlePrintInvoice(javafx.event.ActionEvent event) {
        if (currentOrderList.isEmpty()) {
            AlertUtils.showError("Lỗi", "Không có món nào để xuất hóa đơn!");
            return;
        }
        String htmlContent = generateInvoiceHtmlString();
        showPreviewWindow(htmlContent);
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
        html.append("<p>Bàn: ").append(tableId).append(" | Đơn: #").append(currentOrderId).append("</p>");
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

    private void showPreviewWindow(String htmlContent) {
        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Hóa Đơn - Bàn " + tableId);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(htmlContent);

        Button btnPrint = new Button("Lưu PDF / In");
        btnPrint.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnPrint.setPadding(new Insets(10, 20, 10, 20));

        btnPrint.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(previewStage)) {
                // Khi hộp thoại in hiện ra, chọn 'Microsoft Print to PDF' để lưu file
                webEngine.print(job);
                job.endJob();
            }
        });

        HBox buttonBar = new HBox(15, btnPrint);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setStyle("-fx-background-color: #ecf0f1;");

        BorderPane root = new BorderPane();
        root.setCenter(webView);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 600, 700);
        previewStage.setScene(scene);
        previewStage.show();
    }
}