package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.ShiftDAO;
import com.qlnh.quanlynhahang.dao.UserDAO;
import com.qlnh.quanlynhahang.model.Shift;
import com.qlnh.quanlynhahang.model.User;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox; // Import thêm VBox
import javafx.util.StringConverter;

import java.sql.Date;
import java.time.LocalDate;

public class ShiftManagementController {

    @FXML private VBox formBox; // VBox chứa toàn bộ form nhập liệu (để disable nếu là Staff)
    @FXML private ComboBox<User> cbStaff; // ComboBox chứa Object User
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cbShiftName;
    @FXML private TextField txtNote;

    @FXML private TableView<Shift> tblShifts;
    @FXML private TableColumn<Shift, Date> colDate;
    @FXML private TableColumn<Shift, String> colStaff;
    @FXML private TableColumn<Shift, String> colShiftName;
    @FXML private TableColumn<Shift, String> colNote;

    private final ShiftDAO shiftDAO = new ShiftDAO();
    private final UserDAO userDAO = new UserDAO();

    public void initialize() {
        // Cấu hình ComboBox chọn nhân viên
        cbStaff.setItems(FXCollections.observableArrayList(userDAO.getAllStaff()));

        // Giúp ComboBox hiển thị tên (FullName) thay vì mã Hash
        cbStaff.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User u) {
                return u == null ? "" : u.getFullName() + " (" + u.getUsername() + ")";
            }
            @Override
            public User fromString(String string) { return null; }
        });

        // Cấu hình Ca trực mẫu
        cbShiftName.setItems(FXCollections.observableArrayList(
                "Ca Sáng (8:00 - 12:00)",
                "Ca Chiều (13:00 - 17:00)",
                "Ca Tối (18:00 - 22:00)",
                "Full-time"
        ));
        cbShiftName.getSelectionModel().selectFirst();
        dpDate.setValue(LocalDate.now());

        // Cấu hình bảng
        colDate.setCellValueFactory(new PropertyValueFactory<>("shiftDate"));
        colStaff.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colShiftName.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        loadData();
    }

    // Hàm nhận dữ liệu User từ Dashboard truyền sang
    public void initData(User user) {
        // Nếu là STAFF thì disable toàn bộ form nhập liệu, chỉ cho xem bảng
        if ("STAFF".equalsIgnoreCase(user.getRole())) {
            formBox.setDisable(true);
        }
    }

    private void loadData() {
        tblShifts.setItems(FXCollections.observableArrayList(shiftDAO.getAllShifts()));
    }

    @FXML
    private void handleAdd() {
        User selectedUser = cbStaff.getValue();
        if (selectedUser == null || dpDate.getValue() == null) {
            AlertUtils.showError("Thiếu thông tin", "Vui lòng chọn nhân viên và ngày làm!");
            return;
        }

        Shift shift = new Shift(0,
                selectedUser.getId(),
                selectedUser.getFullName(),
                Date.valueOf(dpDate.getValue()),
                cbShiftName.getValue(),
                txtNote.getText()
        );

        if (shiftDAO.addShift(shift)) {
            AlertUtils.showInfo("Thành công", "Đã phân ca làm việc!");
            loadData();
        } else {
            AlertUtils.showError("Lỗi", "Không thể thêm lịch làm!");
        }
    }

    @FXML
    private void handleDelete() {
        Shift selected = tblShifts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (AlertUtils.showConfirmation("Xác nhận", "Xóa lịch làm việc này?")) {
            if (shiftDAO.deleteShift(selected.getId())) {
                loadData();
            }
        }
    }
}