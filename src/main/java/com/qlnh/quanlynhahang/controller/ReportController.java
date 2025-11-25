package com.qlnh.quanlynhahang.controller;

import com.qlnh.quanlynhahang.dao.ReportDAO;
import com.qlnh.quanlynhahang.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

public class ReportController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private Label lblTotalRevenue;
    @FXML private ComboBox<String> cbReportType;

    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    private final ReportDAO reportDAO = new ReportDAO();

    public void initialize() {
        // Cấu hình mặc định: Từ đầu tháng đến hiện tại
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpTo.setValue(LocalDate.now());

        cbReportType.setItems(FXCollections.observableArrayList("Ngày", "Tuần", "Tháng", "Năm"));
        cbReportType.getSelectionModel().select("Ngày");

        loadData();
    }

    @FXML
    private void handleReload() {
        if (validateInput()) {
            loadData();
        }
    }

    @FXML
    private void handleExport() {
        if (!validateInput()) return;

        // Mở hộp thoại lưu file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Báo Cáo Doanh Thu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel CSV Files", "*.csv"));
        // Tên file gợi ý: BaoCao_Loai_ThoiGian.csv
        String defaultName = "BaoCao_" + cbReportType.getValue() + "_" + System.currentTimeMillis() + ".csv";
        fileChooser.setInitialFileName(defaultName);

        File file = fileChooser.showSaveDialog(lblTotalRevenue.getScene().getWindow());

        if (file != null) {
            exportToCSV(file);
        }
    }

    private void exportToCSV(File file) {
        Date fromDate = Date.valueOf(dpFrom.getValue());
        Date toDate = Date.valueOf(dpTo.getValue());
        String type = cbReportType.getValue();

        Map<String, Double> data = reportDAO.getRevenue(type, fromDate, toDate);

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            // Ghi BOM (Byte Order Mark) để Excel nhận diện đúng Tiếng Việt UTF-8
            writer.write('\ufeff');

            // Ghi tiêu đề cột
            writer.println("Thời gian,Doanh thu (VND)");

            // Ghi dữ liệu
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                // Định dạng số không có phần thập phân cho đẹp
                writer.println(entry.getKey() + "," + String.format("%.0f", entry.getValue()));
            }

            // Ghi tổng cộng
            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            writer.println("TỔNG CỘNG," + String.format("%.0f", total));

            AlertUtils.showInfo("Thành công", "Đã xuất báo cáo thành công!\nBạn có thể mở file bằng Excel.");

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi", "Không thể xuất file: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (dpFrom.getValue() == null || dpTo.getValue() == null) {
            AlertUtils.showError("Lỗi", "Vui lòng chọn đầy đủ Từ ngày và Đến ngày!");
            return false;
        }
        if (dpFrom.getValue().isAfter(dpTo.getValue())) {
            AlertUtils.showError("Lỗi", "Ngày bắt đầu phải nhỏ hơn ngày kết thúc!");
            return false;
        }
        return true;
    }

    private void loadData() {
        Date fromDate = Date.valueOf(dpFrom.getValue());
        Date toDate = Date.valueOf(dpTo.getValue());
        String type = cbReportType.getValue();

        // 1. Lấy dữ liệu từ DAO (Có lọc ngày)
        Map<String, Double> data = reportDAO.getRevenue(type, fromDate, toDate);

        // 2. Tính tổng tiền trong khoảng này
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        lblTotalRevenue.setText(String.format("%,.0f VND", total));

        // 3. Vẽ biểu đồ
        xAxis.setLabel("Thời gian (" + type + ")");
        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu (" + dpFrom.getValue() + " - " + dpTo.getValue() + ")");

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
    }
}