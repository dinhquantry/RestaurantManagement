package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.ToolDAO;
import com.qlnh.quanlynhahang.model.Tool;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ToolController {

    @FXML private TableView<Tool> tblTools;
    @FXML private TableColumn<Tool, String> colName;
    @FXML private TableColumn<Tool, Integer> colQuantity;
    @FXML private TableColumn<Tool, String> colStatus;

    private final ToolDAO toolDAO = new ToolDAO();

    public void initialize() {
        setupTableColumns();
        loadTools();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // SỬA LỖI TẠI ĐÂY: Đổi getThreshold() thành getMinThreshold()
        colStatus.setCellValueFactory(cell -> {
            Tool t = cell.getValue();
            if (t.getQuantity() <= t.getMinThreshold()) {
                return new SimpleStringProperty("CẢNH BÁO: Sắp hết");
            }
            return new SimpleStringProperty("Ổn định");
        });

        // SỬA LỖI TẠI ĐÂY NỮA
        tblTools.setRowFactory(tv -> new TableRow<Tool>() {
            @Override
            protected void updateItem(Tool item, boolean empty) {
                super.updateItem(item, empty);
                // Đổi getThreshold() thành getMinThreshold()
                if (item != null && item.getQuantity() <= item.getMinThreshold()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadTools() {
        List<Tool> dataFromDB = toolDAO.getAllTools();
        ObservableList<Tool> observableList = FXCollections.observableArrayList(dataFromDB);
        tblTools.setItems(observableList);
        checkAlerts(observableList);
    }

    private void checkAlerts(ObservableList<Tool> list) {
        // SỬA LỖI TẠI ĐÂY (Nếu có dùng stream)
        long lowStockCount = list.stream().filter(t -> t.getQuantity() <= t.getMinThreshold()).count();
        if (lowStockCount > 0) {
            AlertUtils.showInfo("Cảnh báo kho dụng cụ",
                    "Có " + lowStockCount + " loại dụng cụ đang dưới mức quy định!");
        }
    }
}