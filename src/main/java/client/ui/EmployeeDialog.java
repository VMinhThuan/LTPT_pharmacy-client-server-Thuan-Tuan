package client.ui;

import client.RMICLientFactory;
import entities.NhanVien;
import entities.TaiKhoan;
import entities.enums.ETinhTrangNhanVien;
import entities.enums.VaiTro;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDialog extends Stage {
    private TextField txtHoTen, txtSdt, txtEmail, txtCccd, txtDiaChi, txtNgayVaoLam;
    private TextField txtTaiKhoan;
    private PasswordField txtPassword;
    private ComboBox<ETinhTrangNhanVien> cbTinhTrang;
    private ComboBox<VaiTro> cbVaiTro;
    private TextField txtChiNhanh;
    private ComboBox<NhanVien> cbQuanLy;
    private NhanVien currentEmployee;
    private MainDashboard dashboard;

    public EmployeeDialog(MainDashboard dashboard, NhanVien nhanVien) {
        this.dashboard = dashboard;
        this.currentEmployee = nhanVien;

        setTitle((nhanVien == null || nhanVien.getMaNhanVien() == null) ? "Thêm Nhân Viên" : "Sửa Nhân Viên");
        initModality(Modality.APPLICATION_MODAL);
        setWidth(500);
        setHeight(600);

        initUI();

        if (nhanVien != null) {
            fillData(nhanVien);
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

        grid.add(new Label("SĐT (*):"), 0, row);
        txtSdt = new TextField();
        grid.add(txtSdt, 1, row++);

        grid.add(new Label("CCCD (*):"), 0, row);
        txtCccd = new TextField();
        grid.add(txtCccd, 1, row++);

        grid.add(new Label("Email (*):"), 0, row);
        txtEmail = new TextField();
        grid.add(txtEmail, 1, row++);

        grid.add(new Label("Địa Chỉ:"), 0, row);
        txtDiaChi = new TextField();
        grid.add(txtDiaChi, 1, row++);

        grid.add(new Label("Ngày Vào Làm (yyyy-MM-dd):"), 0, row);
        txtNgayVaoLam = new TextField(LocalDate.now().toString());
        grid.add(txtNgayVaoLam, 1, row++);

        grid.add(new Label("Tình Trạng:"), 0, row);
        cbTinhTrang = new ComboBox<>();
        cbTinhTrang.getItems().addAll(ETinhTrangNhanVien.values());
        cbTinhTrang.setValue(ETinhTrangNhanVien.DANG_LAM_VIEC);
        grid.add(cbTinhTrang, 1, row++);

        grid.add(new Label("--- THÔNG TIN TÀI KHOẢN ---"), 0, row, 2, 1);
        row++;

        grid.add(new Label("Tên Đăng Nhập (*):"), 0, row);
        txtTaiKhoan = new TextField();
        grid.add(txtTaiKhoan, 1, row++);
        if (currentEmployee != null && currentEmployee.getTaiKhoan() != null) {
            txtTaiKhoan.setDisable(true); // Không cho sửa tên tài khoản khi đã tạo
        }

        grid.add(new Label("Mật Khẩu (*):"), 0, row);
        txtPassword = new PasswordField();
        grid.add(txtPassword, 1, row++);

        grid.add(new Label("Vai Trò:"), 0, row);
        cbVaiTro = new ComboBox<>();
        cbVaiTro.getItems().addAll(VaiTro.values());
        cbVaiTro.setValue(VaiTro.NHANVIEN);
        grid.add(cbVaiTro, 1, row++);

        grid.add(new Label("Chi Nhánh:"), 0, row);
        txtChiNhanh = new TextField();
        txtChiNhanh.setPromptText("VD: Chi nhánh Quận 1");
        grid.add(txtChiNhanh, 1, row++);

        grid.add(new Label("Quản lý trực tiếp:"), 0, row);
        cbQuanLy = new ComboBox<>();
        cbQuanLy.setPrefWidth(250);
        cbQuanLy.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NhanVien item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getHoTen() + " (" + item.getMaNhanVien() + ")");
            }
        });
        cbQuanLy.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(NhanVien item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getHoTen() + " (" + item.getMaNhanVien() + ")");
            }
        });
        grid.add(cbQuanLy, 1, row++);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15));

        Button btnSave = new Button("Lưu");
        btnSave.setOnAction(e -> saveEmployee());

        Button btnCancel = new Button("Hủy");
        btnCancel.setOnAction(e -> close());

        btnBox.getChildren().addAll(btnSave, btnCancel);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(grid);
        mainPane.setBottom(btnBox);

        Scene scene = new Scene(mainPane);
        setScene(scene);
        loadManagerData();
        setupRoleUiBehavior();
    }

    private void fillData(NhanVien nv) {
        txtHoTen.setText(nv.getHoTen());
        txtSdt.setText(nv.getSdt());
        txtCccd.setText(nv.getCccd());
        txtEmail.setText(nv.getEmail());
        txtDiaChi.setText(nv.getDiaChi());
        txtNgayVaoLam.setText(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().toString() : LocalDate.now().toString());
        cbTinhTrang.setValue(nv.getTinhTrangNhanVien());

        if (nv.getTaiKhoan() != null) {
            txtTaiKhoan.setText(nv.getTaiKhoan().getMaTaiKhoan());
            txtPassword.clear();
            txtPassword.setPromptText("Để trống nếu không đổi mật khẩu");
            cbVaiTro.setValue(nv.getTaiKhoan().getVaiTro());
        }
        txtChiNhanh.setText(nv.getChiNhanh() != null ? nv.getChiNhanh() : "");
        if (nv.getQuanLy() != null) {
            cbQuanLy.setValue(nv.getQuanLy());
        }
    }

    private void loadManagerData() {
        try {
            List<NhanVien> all = RMICLientFactory.getEmployeeService().getAll();
            List<NhanVien> managers = new ArrayList<>();
            if (all != null) {
                for (NhanVien nv : all) {
                    if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getVaiTro() == VaiTro.QUANLY) {
                        managers.add(nv);
                    }
                }
            }
            cbQuanLy.getItems().setAll(managers);
        } catch (Exception ignored) {
        }
    }

    private void setupRoleUiBehavior() {
        TaiKhoan loggedAccount = dashboard.getLoggedInAccount();
        VaiTro loggedRole = loggedAccount != null ? loggedAccount.getVaiTro() : null;
        NhanVien loggedNv = loggedAccount != null ? loggedAccount.getNhanVien() : null;
        boolean isManagerUser = loggedRole == VaiTro.QUANLY;

        if (isManagerUser && loggedNv != null) {
            txtChiNhanh.setText(loggedNv.getChiNhanh() != null ? loggedNv.getChiNhanh() : "");
            txtChiNhanh.setDisable(true);
            cbVaiTro.getItems().setAll(VaiTro.NHANVIEN);
            cbVaiTro.setValue(VaiTro.NHANVIEN);
            cbVaiTro.setDisable(true);
            cbQuanLy.setValue(loggedNv);
            cbQuanLy.setDisable(true);
        }

        cbVaiTro.valueProperty().addListener((obs, oldV, newV) -> {
            boolean isManager = newV == VaiTro.QUANLY;
            cbQuanLy.setDisable(isManager || isManagerUser);
            if (isManager) {
                cbQuanLy.setValue(null);
            }
        });
    }

    private String getSafeText(TextField txt) {
        if (txt == null || txt.getText() == null) return "";
        return txt.getText().trim();
    }

    private void saveEmployee() {
        try {
            if (getSafeText(txtHoTen).isEmpty() || getSafeText(txtSdt).isEmpty() || getSafeText(txtCccd).isEmpty() || getSafeText(txtEmail).isEmpty() || getSafeText(txtTaiKhoan).isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ các trường bắt buộc (*)");
                return;
            }

            String phone = getSafeText(txtSdt);
            if (!phone.matches("^0\\d{9}$")) {
                showAlert(Alert.AlertType.WARNING, "Sai định dạng", "Số điện thoại không hợp lệ! Vui lòng nhập 10 số bắt đầu bằng số 0.");
                return;
            }

            NhanVien nv = (currentEmployee == null) ? new NhanVien() : currentEmployee;

            nv.setHoTen(getSafeText(txtHoTen));
            nv.setSdt(phone);
            nv.setCccd(getSafeText(txtCccd));
            nv.setEmail(getSafeText(txtEmail));
            nv.setDiaChi(getSafeText(txtDiaChi));
            nv.setNgayVaoLam(LocalDate.parse(getSafeText(txtNgayVaoLam), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            nv.setTinhTrangNhanVien(cbTinhTrang.getValue());

            // Handle Account
            TaiKhoan tk = nv.getTaiKhoan();
            boolean isNewAccount = false;
            if (tk == null) {
                tk = new TaiKhoan();
                tk.setMaTaiKhoan(getSafeText(txtTaiKhoan));
                isNewAccount = true;
            }
            String rawPassword = getSafeText(txtPassword);
            if (isNewAccount && rawPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập mật khẩu cho tài khoản mới.");
                return;
            }
            if (!rawPassword.isEmpty()) {
                tk.setPassword(rawPassword);
            }
            tk.setVaiTro(cbVaiTro.getValue());
            if (getSafeText(txtChiNhanh).isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập chi nhánh.");
                return;
            }
            if (tk.getVaiTro() == VaiTro.NHANVIEN && cbQuanLy.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Nhân viên phải thuộc một quản lý trực tiếp.");
                return;
            }

            // Because there's no Cascade ALL on NhanVien to TaiKhoan, we have to save TaiKhoan first
            if (isNewAccount) {
                boolean accountSaved = RMICLientFactory.getAccountService().save(tk);
                if (!accountSaved) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên đăng nhập đã tồn tại hoặc lỗi khi tạo tài khoản!");
                    return;
                }
            } else {
                RMICLientFactory.getAccountService().update(tk);
            }

            nv.setTaiKhoan(tk);
            nv.setChiNhanh(getSafeText(txtChiNhanh));
            if (tk.getVaiTro() == VaiTro.QUANLY) {
                nv.setQuanLy(null);
            } else {
                nv.setQuanLy(cbQuanLy.getValue());
            }

            boolean success;
            if (nv.getMaNhanVien() == null) {
                success = RMICLientFactory.getEmployeeService().save(nv);
            } else {
                success = RMICLientFactory.getEmployeeService().update(nv);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu nhân viên thành công!");
                dashboard.refreshEmployeeList();
                close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lưu nhân viên thất bại!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi: " + ex.getMessage());
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
