package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.ToolDAO;
import com.qlnh.quanlynhahang.model.Tool;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ToolController {

    @FXML private TextField txtName;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtThreshold;

    @FXML private TableView<Tool> tblTools;
    @FXML private TableColumn<Tool, String> colName;
    @FXML private TableColumn<Tool, Integer> colQuantity;
    @FXML private TableColumn<Tool, String> colStatus;

    private final ToolDAO toolDAO = new ToolDAO();

    public void initialize() {
        setupTableColumns();
        loadTools();

        // Sự kiện click vào bảng để đổ dữ liệu lên form
        tblTools.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colStatus.setCellValueFactory(cell -> {
            Tool t = cell.getValue();
            if (t.getQuantity() <= t.getMinThreshold()) {
                return new SimpleStringProperty("CẢNH BÁO: Sắp hết (Min: " + t.getMinThreshold() + ")");
            }
            return new SimpleStringProperty("Ổn định");
        });

        // Tô màu đỏ dòng cảnh báo
        tblTools.setRowFactory(tv -> new TableRow<Tool>() {
            @Override
            protected void updateItem(Tool item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.getQuantity() <= item.getMinThreshold()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadTools() {
        ObservableList<Tool> list = FXCollections.observableArrayList(toolDAO.getAllTools());
        tblTools.setItems(list);
    }

    private void fillForm(Tool t) {
        txtName.setText(t.getName());
        txtQuantity.setText(String.valueOf(t.getQuantity()));
        txtThreshold.setText(String.valueOf(t.getMinThreshold()));
    }

    @FXML
    private void handleAdd() {
        if (!validateInput()) return;

        // ID = 0 vì DB tự tăng, Status mặc định "Tốt"
        Tool t = new Tool(0, txtName.getText(),
                Integer.parseInt(txtQuantity.getText()),
                Integer.parseInt(txtThreshold.getText()),
                "Tốt");

        if (toolDAO.addTool(t)) {
            AlertUtils.showInfo("Thành công", "Đã thêm dụng cụ mới!");
            loadTools();
            handleClear();
        } else {
            AlertUtils.showError("Lỗi", "Thêm thất bại!");
        }
    }

    @FXML
    private void handleUpdate() {
        Tool selected = tblTools.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Lỗi", "Vui lòng chọn dụng cụ cần sửa!");
            return;
        }
        if (!validateInput()) return;

        selected.setName(txtName.getText());
        selected.setQuantity(Integer.parseInt(txtQuantity.getText()));
        selected.setMinThreshold(Integer.parseInt(txtThreshold.getText()));

        if (toolDAO.updateTool(selected)) {
            AlertUtils.showInfo("Thành công", "Cập nhật thành công!");
            loadTools();
            tblTools.refresh();
        } else {
            AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
        }
    }

    @FXML
    private void handleDelete() {
        Tool selected = tblTools.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (AlertUtils.showConfirmation("Xác nhận", "Bạn có chắc muốn xóa: " + selected.getName() + "?")) {
            if (toolDAO.deleteTool(selected.getId())) {
                AlertUtils.showInfo("Thành công", "Đã xóa!");
                loadTools();
                handleClear();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtName.clear();
        txtQuantity.clear();
        txtThreshold.clear();
        tblTools.getSelectionModel().clearSelection();
    }

    private boolean validateInput() {
        if (txtName.getText().isEmpty() || txtQuantity.getText().isEmpty() || txtThreshold.getText().isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        try {
            Integer.parseInt(txtQuantity.getText());
            Integer.parseInt(txtThreshold.getText());
        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi", "Số lượng và Mức cảnh báo phải là số nguyên!");
            return false;
        }
        return true;
    }
}