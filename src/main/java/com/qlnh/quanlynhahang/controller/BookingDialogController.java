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
    @FXML private TextField txtGuestCount; // Mới thêm: TextField nhập số lượng khách
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

        // Tự động chọn giờ gần nhất hoặc giờ mở cửa
        selectNearestTime();
    }

    private void selectNearestTime() {
        LocalTime now = LocalTime.now();
        if (now.getHour() >= 8 && now.getHour() <= 21) {
            String currentHour = String.format("%02d:00", now.getHour() + 1); // Gợi ý giờ tiếp theo
            if (cbTime.getItems().contains(currentHour)) {
                cbTime.setValue(currentHour);
            } else {
                cbTime.getSelectionModel().selectFirst();
            }
        } else {
            cbTime.getSelectionModel().selectFirst();
        }
    }

    public void setTable(DiningTable table) {
        this.currentTable = table;
        if (table != null) {
            lblTableName.setText("Đặt cho bàn: " + table.getName() + " (Sức chứa: " + table.getCapacity() + ")");
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputInvalid()) {
            return;
        }

        try {
            // 1. Xử lý thời gian
            LocalTime time = LocalTime.parse(cbTime.getValue());
            LocalDateTime bookingDateTime = LocalDateTime.of(dpDate.getValue(), time);

            // Kiểm tra thời gian đặt có trong quá khứ không
            if (bookingDateTime.isBefore(LocalDateTime.now())) {
                AlertUtils.showError("Lỗi thời gian", "Thời gian đặt bàn phải sau thời điểm hiện tại!");
                return;
            }

            // 2. Xử lý số lượng khách
            int guestCount = Integer.parseInt(txtGuestCount.getText().trim());
            if (guestCount <= 0) {
                AlertUtils.showError("Lỗi nhập liệu", "Số lượng khách phải lớn hơn 0!");
                return;
            }

            // Cảnh báo nhẹ nếu số khách vượt quá sức chứa bàn (tùy chọn)
            if (currentTable != null && guestCount > currentTable.getCapacity()) {
                // Có thể hiện Confirm Dialog ở đây nếu muốn, hoặc chỉ thông báo lỗi
                // Ở đây mình để thông báo lỗi chặn luôn để đảm bảo quy trình
                AlertUtils.showError("Quá tải", "Số khách vượt quá sức chứa của bàn (" + currentTable.getCapacity() + " người)!");
                return;
            }

            // 3. Tạo đối tượng Booking
            Booking b = new Booking();
            b.setCustomerName(txtName.getText().trim());
            b.setPhone(txtPhone.getText().trim());
            b.setTableId(currentTable.getId());
            b.setBookingTime(Timestamp.valueOf(bookingDateTime));
            b.setGuestCount(guestCount); // Giả sử model Booking đã có setter này
            b.setStatus("CONFIRMED"); // Trạng thái mặc định

            // 4. Lưu vào CSDL
            if (bookingDAO.createBooking(b)) {
                // Cập nhật trạng thái bàn sang BOOKED
                tableDAO.updateStatus(currentTable.getId(), "BOOKED");

                AlertUtils.showInfo("Thành công", "Đã đặt bàn thành công!");
                saveClicked = true;
                closeDialog();
            } else {
                AlertUtils.showError("Lỗi hệ thống", "Không thể lưu thông tin đặt bàn!");
            }

        } catch (NumberFormatException e) {
            AlertUtils.showError("Lỗi nhập liệu", "Số lượng khách phải là số nguyên!");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi không xác định", "Chi tiết: " + e.getMessage());
        }
    }

    private boolean isInputInvalid() {
        String error = "";
        if (txtName.getText().trim().isEmpty()) error += "Chưa nhập tên khách hàng.\n";
        if (txtPhone.getText().trim().isEmpty()) error += "Chưa nhập số điện thoại.\n";
        if (txtGuestCount.getText().trim().isEmpty()) error += "Chưa nhập số lượng khách.\n";
        if (dpDate.getValue() == null) error += "Chưa chọn ngày.\n";
        if (cbTime.getValue() == null) error += "Chưa chọn giờ.\n";

        if (!error.isEmpty()) {
            AlertUtils.showError("Thiếu thông tin", error);
            return true;
        }
        return false;
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