package com.qlnh.quanlynhahang.controller;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class InvoicePreviewController {

    @FXML private WebView webView;
    private WebEngine webEngine;

    public void initialize() {
        webEngine = webView.getEngine();
    }

    // Hàm nhận nội dung HTML từ OrderController truyền sang
    public void setInvoiceContent(String htmlContent) {
        webEngine.loadContent(htmlContent);
    }

    @FXML
    private void handlePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(webView.getScene().getWindow())) {
            // In nội dung WebView -> Chọn 'Microsoft Print to PDF' để lưu file
            webEngine.print(job);
            job.endJob();
        }
    }
}