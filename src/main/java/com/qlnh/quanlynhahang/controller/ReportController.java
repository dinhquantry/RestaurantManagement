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

// Import Apache POI
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportController {

    // ... (Các khai báo @FXML cũ giữ nguyên) ...
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private Label lblTotalRevenue;
    @FXML private ComboBox<String> cbReportType;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    private final ReportDAO reportDAO = new ReportDAO();

    public void initialize() {
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpTo.setValue(LocalDate.now());
        cbReportType.setItems(FXCollections.observableArrayList("Ngày", "Tuần", "Tháng", "Năm"));
        cbReportType.getSelectionModel().select("Ngày");
        loadData();
    }

    @FXML
    private void handleReload() {
        if (validateInput()) loadData();
    }

    @FXML
    private void handleExport() {
        if (!validateInput()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Báo Cáo Doanh Thu");

        // CHỈNH SỬA 1: Đổi đuôi file sang .xlsx
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        String defaultName = "BaoCao_" + cbReportType.getValue() + "_" + System.currentTimeMillis() + ".xlsx";
        fileChooser.setInitialFileName(defaultName);

        File file = fileChooser.showSaveDialog(lblTotalRevenue.getScene().getWindow());

        if (file != null) {
            exportToExcel(file); // Gọi hàm xuất Excel mới
        }
    }

    // CHỈNH SỬA 2: Hàm xuất Excel chuyên nghiệp
    private void exportToExcel(File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu");

            // --- 1. TẠO CÁC STYLE (Định dạng) ---

            // Style cho Tiêu đề lớn (Title)
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style cho Header bảng (In đậm, nền xám, có viền)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style cho Dữ liệu thường (Có viền)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Style cho Tiền tệ (Có viền + Định dạng số #,##0)
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0")); // Định dạng 1,000,000

            // Style cho Tổng cộng (In đậm, màu cam nhạt)
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(currencyStyle);
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // --- 2. ĐỔ DỮ LIỆU ---

            int rowNum = 0;

            // Dòng 1: Tiêu đề lớn
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU NHÀ HÀNG");
            titleCell.setCellStyle(titleStyle);
            // Merge cells cho tiêu đề (Từ cột A đến B)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            // Dòng 2: Thời gian báo cáo
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateCell.setCellValue("Thời gian: " + dpFrom.getValue().format(formatter) + " - " + dpTo.getValue().format(formatter));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

            rowNum++; // Bỏ trống 1 dòng cho thoáng

            // Dòng 4: Header Bảng
            Row headerRow = sheet.createRow(rowNum++);
            Cell h1 = headerRow.createCell(0); h1.setCellValue("Thời Gian"); h1.setCellStyle(headerStyle);
            Cell h2 = headerRow.createCell(1); h2.setCellValue("Doanh Thu (VNĐ)"); h2.setCellStyle(headerStyle);

            // Lấy dữ liệu
            Date fromDate = Date.valueOf(dpFrom.getValue());
            Date toDate = Date.valueOf(dpTo.getValue());
            Map<String, Double> data = reportDAO.getRevenue(cbReportType.getValue(), fromDate, toDate);
            double totalRevenue = 0;

            // Dòng dữ liệu
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                Row row = sheet.createRow(rowNum++);

                Cell c1 = row.createCell(0);
                c1.setCellValue(entry.getKey());
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(1);
                c2.setCellValue(entry.getValue()); // Lưu dạng số, không phải String
                c2.setCellStyle(currencyStyle);

                totalRevenue += entry.getValue();
            }

            // Dòng Tổng cộng
            Row totalRow = sheet.createRow(rowNum++);
            Cell t1 = totalRow.createCell(0);
            t1.setCellValue("TỔNG CỘNG");
            t1.setCellStyle(totalStyle);

            Cell t2 = totalRow.createCell(1);
            t2.setCellValue(totalRevenue);
            t2.setCellStyle(totalStyle);

            // Auto-size cột cho vừa nội dung
            sheet.autoSizeColumn(0);
            sheet.setColumnWidth(1, 5000); // Cột tiền rộng hơn chút (đơn vị 1/256th character width)

            // --- 3. LƯU FILE ---
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                AlertUtils.showInfo("Thành công", "Đã xuất file Excel chuẩn thành công!");
            }

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
        // ... (Giữ nguyên logic loadData cũ) ...
        Date fromDate = Date.valueOf(dpFrom.getValue());
        Date toDate = Date.valueOf(dpTo.getValue());
        String type = cbReportType.getValue();

        Map<String, Double> data = reportDAO.getRevenue(type, fromDate, toDate);
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        lblTotalRevenue.setText(String.format("%,.0f VND", total));

        xAxis.setLabel("Thời gian (" + type + ")");
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(series);
    }
}