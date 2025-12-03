package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.TableDAO;
import com.qlnh.quanlynhahang.model.DiningTable;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TableDialogController {

    @FXML private Label lblTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtCapacity;
    @FXML private ComboBox<Integer> cbFloor;

    private DiningTable currentTable;
    private final TableDAO tableDAO = new TableDAO();
    private boolean saveClicked = false;

    public void initialize() {
        // Khởi tạo danh sách tầng (Ví dụ 3 tầng)
        cbFloor.setItems(FXCollections.observableArrayList(1, 2, 3));
        cbFloor.getSelectionModel().selectFirst();
    }

    public void setTable(DiningTable table) {
        this.currentTable = table;

        if (table != null) {
            // Chế độ Sửa
            lblTitle.setText("CẬP NHẬT BÀN");
            txtName.setText(table.getName());
            txtCapacity.setText(String.valueOf(table.getCapacity()));
            cbFloor.getSelectionModel().select(Integer.valueOf(table.getFloor()));
        } else {
            // Chế độ Thêm mới
            lblTitle.setText("THÊM BÀN MỚI");
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtCapacity.getText().isEmpty()) {
            AlertUtils.showError("Lỗi", "Vui lòng nhập tên bàn và số ghế!");
            return;
        }

        try {
            int capacity = Integer.parseInt(txtCapacity.getText());
            int floor = cbFloor.getValue();

            if (currentTable == null) {
                // Thêm mới (Status mặc định là EMPTY được xử lý trong DAO)
                DiningTable newTable = new DiningTable(0, txtName.getText(), capacity, "EMPTY", floor);
                if (tableDAO.addTable(newTable)) {
                    saveClicked = true;
                    closeDialog();
                } else {
                    AlertUtils.showError("Lỗi", "Thêm thất bại!");
                }
            } else {
                // Cập nhật
                currentTable.setName(txtName.getText());
                currentTable.setCapacity(capacity);
                currentTable.setFloor(floor);

                if (tableDAO.updateTableInfo(currentTable)) {
                    saveClicked = true;
                    closeDialog();
                } else {
                    AlertUtils.showError("Lỗi", "Cập nhật thất bại!");
                }
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi", "Số ghế phải là số nguyên!");
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