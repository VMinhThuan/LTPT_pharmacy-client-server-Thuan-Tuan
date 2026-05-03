package client.ui;

import client.RMICLientFactory;
import entities.KhachHang;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomerDialog extends Stage {
    private TextField txtHoTen, txtNgaySinh, txtSdt, txtDiemTichLuy;
    private ComboBox<String> cbGioiTinh;
    private KhachHang currentCustomer;
    private MainDashboard dashboard;

    public CustomerDialog(MainDashboard dashboard, KhachHang khachHang) {
        this.dashboard = dashboard;
        this.currentCustomer = khachHang;

        setTitle((khachHang == null || khachHang.getMaKhachHang() == null) ? "Thêm Khách Hàng" : "Sửa Khách Hàng");
        initModality(Modality.APPLICATION_MODAL);
        setWidth(400);
        setHeight(400);

        initUI();

        if (khachHang != null) {
            fillData(khachHang);
        }
    }

    private void initUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        int row = 0;

        grid.add(new Label("Họ Tên (*):"), 0, row);
        txtHoTen = new TextField();
        grid.add(txtHoTen, 1, row++);

        grid.add(new Label("Ngày Sinh (yyyy-MM-dd) (*):"), 0, row);
        txtNgaySinh = new TextField(LocalDate.now().minusYears(20).toString());
        grid.add(txtNgaySinh, 1, row++);

        grid.add(new Label("Giới Tính:"), 0, row);
        cbGioiTinh = new ComboBox<>();
        cbGioiTinh.getItems().addAll("Nam", "Nữ");
        cbGioiTinh.setValue("Nam");
        grid.add(cbGioiTinh, 1, row++);

        grid.add(new Label("Số Điện Thoại (*):"), 0, row);
        txtSdt = new TextField();
        grid.add(txtSdt, 1, row++);

        grid.add(new Label("Điểm Tích Lũy:"), 0, row);
        txtDiemTichLuy = new TextField("0");
        txtDiemTichLuy.setEditable(false);
        txtDiemTichLuy.setDisable(true);
        grid.add(txtDiemTichLuy, 1, row++);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15));

        Button btnSave = new Button("Lưu");
        btnSave.setOnAction(e -> saveCustomer());

        Button btnCancel = new Button("Hủy");
        btnCancel.setOnAction(e -> close());

        btnBox.getChildren().addAll(btnSave, btnCancel);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(grid);
        mainPane.setBottom(btnBox);

        Scene scene = new Scene(mainPane);
        setScene(scene);
    }

    private void fillData(KhachHang kh) {
        txtHoTen.setText(kh.getHoTen());
        txtNgaySinh.setText(kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : LocalDate.now().minusYears(20).toString());
        cbGioiTinh.setValue(kh.isGioiTinh() ? "Nữ" : "Nam");
        txtSdt.setText(kh.getSoDienThoai());
        txtDiemTichLuy.setText(String.valueOf(kh.getDiemTichLuy()));
    }

    private String getSafeText(TextField txt) {
        if (txt == null || txt.getText() == null) return "";
        return txt.getText().trim();
    }

    private void saveCustomer() {
        try {
            if (getSafeText(txtHoTen).isEmpty() || getSafeText(txtNgaySinh).isEmpty() || getSafeText(txtSdt).isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ các trường bắt buộc (*)");
                return;
            }

            String phone = getSafeText(txtSdt);
            if (!phone.matches("^0\\d{9}$")) {
                showAlert(Alert.AlertType.WARNING, "Sai định dạng", "Số điện thoại không hợp lệ! Vui lòng nhập 10 số bắt đầu bằng số 0.");
                return;
            }

            KhachHang kh = (currentCustomer == null) ? new KhachHang() : currentCustomer;

            kh.setHoTen(getSafeText(txtHoTen));
            kh.setNgaySinh(LocalDate.parse(getSafeText(txtNgaySinh), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            kh.setGioiTinh(cbGioiTinh.getValue().equals("Nữ"));
            kh.setSoDienThoai(phone);
            
            String diemTxt = getSafeText(txtDiemTichLuy);
            kh.setDiemTichLuy(diemTxt.isEmpty() ? 0 : Double.parseDouble(diemTxt));

            if (kh.getMaKhachHang() == null) {
                kh.setNgayThamGia(LocalDate.now()); // Default cho khách mới
            }

            boolean success;
            if (kh.getMaKhachHang() == null) {
                success = RMICLientFactory.getKhachHangService().save(kh);
            } else {
                success = RMICLientFactory.getKhachHangService().update(kh);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu khách hàng thành công!");
                dashboard.refreshCustomerList();
                close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lưu khách hàng thất bại!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Lỗi: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
