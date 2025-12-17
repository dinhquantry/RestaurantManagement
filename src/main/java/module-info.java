module com.qlnh.quanlynhahang {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.web;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.qlnh.quanlynhahang.controller to javafx.fxml;
    opens com.qlnh.quanlynhahang.model to javafx.base;
    opens com.qlnh.quanlynhahang to javafx.fxml;

    exports com.qlnh.quanlynhahang;
}