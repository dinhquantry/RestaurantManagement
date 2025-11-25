module com.qlnh.quanlynhahang {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.qlnh.quanlynhahang.controller to javafx.fxml;
    opens com.qlnh.quanlynhahang.model to javafx.base;
    opens com.qlnh.quanlynhahang to javafx.fxml;

    exports com.qlnh.quanlynhahang;
}