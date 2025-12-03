package com.qlnh.quanlynhahang;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Load màn hình Đăng nhập đầu tiên
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        scene = new Scene(root);
        stage.setScene(scene);

        // --- THIẾT LẬP ICON CHO ỨNG DỤNG (Taskbar & Title bar) ---
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/Logo.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.out.println("Không tìm thấy file !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ---------------------------------------------------------

        stage.setTitle("Hệ Thống Quản Lý Nhà Hàng");
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}