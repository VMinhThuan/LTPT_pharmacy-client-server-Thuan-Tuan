package client.ui;

import client.RMICLientFactory;
import entities.NhanVien;
import entities.TaiKhoan;
import entities.enums.ETinhTrangNhanVien;
import entities.enums.VaiTro;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;

public class LoginUI extends Application {

    private Stage window;
    private Scene loginScene, signupScene;

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        window.setTitle("Pharmacy Management System");

        createLoginScene();
        createSignupScene();

        window.setScene(loginScene);
        window.setResizable(false);
        window.show();
    }

    private void createLoginScene() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-image: url('https://static.toiimg.com/thumb/msid-123946483,width-1280,height-720,resizemode-4/123946483.jpg');" +
                      "-fx-background-size: cover;" +
                      "-fx-background-position: center center;");

        VBox formBox = new VBox(20);
        formBox.setMaxWidth(400);
        formBox.setPadding(new Insets(40));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);");

        Label title = new Label("PHARMACY LOGIN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#2c3e50"));

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Tên đăng nhập");
        styleInput(txtUsername);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Mật khẩu");
        styleInput(txtPassword);

        Button btnLogin = new Button("ĐĂNG NHẬP");
        stylePrimaryButton(btnLogin);
        btnLogin.setOnAction(e -> handleLogin(txtUsername.getText(), txtPassword.getText()));

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        Label lblNoAccount = new Label("Chưa có tài khoản?");
        lblNoAccount.setTextFill(Color.web("#7f8c8d"));
        Hyperlink linkSignup = new Hyperlink("Đăng ký ngay");
        linkSignup.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        linkSignup.setOnAction(e -> window.setScene(signupScene));
        bottomBox.getChildren().addAll(lblNoAccount, linkSignup);

        formBox.getChildren().addAll(title, txtUsername, txtPassword, btnLogin, bottomBox);
        root.getChildren().add(formBox);

        loginScene = new Scene(root, 600, 500);
    }

    private void createSignupScene() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-image: url('https://static.toiimg.com/thumb/msid-123946483,width-1280,height-720,resizemode-4/123946483.jpg');" +
                      "-fx-background-size: cover;" +
                      "-fx-background-position: center center;");

        VBox formBox = new VBox(15);
        formBox.setMaxWidth(400);
        formBox.setPadding(new Insets(30, 40, 30, 40));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);");

        Label title = new Label("ĐĂNG KÝ TÀI KHOẢN");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#27ae60"));

        TextField txtName = new TextField();
        txtName.setPromptText("Họ và Tên");
        styleInput(txtName);

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Số điện thoại");
        styleInput(txtPhone);

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Tên đăng nhập");
        styleInput(txtUsername);

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Mật khẩu");
        styleInput(txtPassword);

        Button btnSignup = new Button("ĐĂNG KÝ");
        stylePrimaryButton(btnSignup);
        btnSignup.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        
        btnSignup.setOnAction(e -> handleSignup(txtName.getText(), txtPhone.getText(), txtUsername.getText(), txtPassword.getText()));

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        Label lblHasAccount = new Label("Đã có tài khoản?");
        lblHasAccount.setTextFill(Color.web("#7f8c8d"));
        Hyperlink linkLogin = new Hyperlink("Đăng nhập");
        linkLogin.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        linkLogin.setOnAction(e -> window.setScene(loginScene));
        bottomBox.getChildren().addAll(lblHasAccount, linkLogin);

        formBox.getChildren().addAll(title, txtName, txtPhone, txtUsername, txtPassword, btnSignup, bottomBox);
        root.getChildren().add(formBox);

        signupScene = new Scene(root, 600, 500);
    }

    private void styleInput(TextField txt) {
        txt.setStyle("-fx-background-color: #f1f2f6; -fx-border-color: #ced6e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14px;");
        txt.setPrefHeight(40);
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.9));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            TaiKhoan taiKhoan = RMICLientFactory.getAccountService().logIn(username, password);

            if (taiKhoan != null) {
                window.close();
                MainDashboard dashboard = new MainDashboard();
                Stage dashboardStage = new Stage();
                dashboard.start(dashboardStage);
                dashboard.setLoggedInAccount(taiKhoan);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", "Sai tên đăng nhập hoặc mật khẩu!");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi kết nối tới Server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleSignup(String name, String phone, String username, String password) {
        if (name.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try {
            // Check if username already exists
            TaiKhoan tk = new TaiKhoan();
            tk.setMaTaiKhoan(username);
            tk.setPassword(password);
            tk.setVaiTro(VaiTro.NHANVIEN);

            boolean accountSaved = RMICLientFactory.getAccountService().save(tk);
            if (!accountSaved) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập đã tồn tại!");
                return;
            }

            NhanVien nv = new NhanVien();
            nv.setHoTen(name);
            nv.setSdt(phone);
            nv.setCccd("000000000000"); // Default since it wasn't captured in signup
            nv.setEmail("not_provided@example.com");
            nv.setDiaChi("Chưa cập nhật");
            nv.setNgayVaoLam(LocalDate.now());
            nv.setTinhTrangNhanVien(ETinhTrangNhanVien.DANG_LAM_VIEC);
            nv.setTaiKhoan(tk);

            boolean nvSaved = RMICLientFactory.getEmployeeService().save(nv);
            if (nvSaved) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công! Vui lòng đăng nhập.");
                window.setScene(loginScene); // Switch back to login
            } else {
                RMICLientFactory.getAccountService().delete(username); // rollback account
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Đăng ký nhân viên thất bại!");
            }

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi kết nối tới Server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
