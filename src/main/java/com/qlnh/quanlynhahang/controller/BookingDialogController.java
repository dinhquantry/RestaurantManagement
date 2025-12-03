package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.BookingDAO;
import com.qlnh.quanlynhahang.dao.TableDAO;
import com.qlnh.quanlynhahang.model.Booking;
import com.qlnh.quanlynhahang.model.DiningTable;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingDialogController {

    @FXML private Label lblTableName;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbTime;

    private DiningTable currentTable;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final TableDAO tableDAO = new TableDAO();
    private boolean saveClicked = false;

    public void initialize() {
        dpDate.setValue(LocalDate.now());

        // Tạo danh sách giờ từ 08:00 đến 22:00
        for (int h = 8; h <= 22; h++) {
            cbTime.getItems().add(String.format("%02d:00", h));
            cbTime.getItems().add(String.format("%02d:30", h));
        }
        cbTime.getSelectionModel().selectFirst();
    }

    public void setTable(DiningTable table) {
        this.currentTable = table;
        if (table != null) {
            lblTableName.setText("Đặt cho bàn: " + table.getName());
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtPhone.getText().isEmpty() || dpDate.getValue() == null || cbTime.getValue() == null) {
            AlertUtils.showError("Thiếu thông tin", "Vui lòng nhập đầy đủ tên, sđt và thời gian!");
            return;
        }

        try {
            LocalTime time = LocalTime.parse(cbTime.getValue());
            LocalDateTime dateTime = LocalDateTime.of(dpDate.getValue(), time);

            Booking b = new Booking();
            b.setCustomerName(txtName.getText());
            b.setPhone(txtPhone.getText());
            b.setTableId(currentTable.getId());
            b.setBookingTime(Timestamp.valueOf(dateTime));

            if (bookingDAO.createBooking(b)) {
                // Cập nhật trạng thái bàn sang BOOKED
                tableDAO.updateStatus(currentTable.getId(), "BOOKED");

                AlertUtils.showInfo("Thành công", "Đã đặt bàn thành công!");
                saveClicked = true;
                closeDialog();
            } else {
                AlertUtils.showError("Lỗi", "Không thể lưu thông tin đặt bàn!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Định dạng thời gian không hợp lệ!");
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