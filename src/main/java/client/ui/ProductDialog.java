package client.ui;

import client.RMICLientFactory;
import entities.DonViTinh;
import entities.NhomThuoc;
import entities.SanPham;
import entities.enums.ELoaiSanPham;
import entities.enums.ETinhTrangSP;
import service.SanPhamService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class ProductDialog extends Stage {
    private TextField txtTenSP, txtKhoiLuong, txtNuocSX, txtThuongHieu, txtGiaBan, txtTacDungPhu, txtLoiKhuyen, txtCongDung, txtNguongTon;
    private DatePicker dpNgaySX, dpHanSD;
    private TextArea txtMoTa;
    private ComboBox<ETinhTrangSP> cbTinhTrang;
    private ComboBox<ELoaiSanPham> cbLoaiSP;
    private ComboBox<DonViTinh> cbDonViTinh;
    private ComboBox<NhomThuoc> cbNhomThuoc;
    private CheckBox chkThuocKeDon;

    private SanPham currentSanPham;
    private MainDashboard dashboard;

    public ProductDialog(MainDashboard dashboard, SanPham sanPham) {
        this.dashboard = dashboard;
        this.currentSanPham = sanPham;
        
        setTitle(sanPham == null ? "Thêm Sản Phẩm Mới" : "Sửa Sản Phẩm");
        initModality(Modality.APPLICATION_MODAL);
        setWidth(600);
        setHeight(750);
        
        initUI();
        loadComboData();
        
        if (sanPham != null) {
            fillData(sanPham);
        }
    }

    private void initUI() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        int row = 0;
        
        grid.add(new Label("Tên Sản Phẩm (*):"), 0, row);
        txtTenSP = new TextField();
        grid.add(txtTenSP, 1, row++);

        grid.add(new Label("Ngày SX:"), 0, row);
        dpNgaySX = new DatePicker(LocalDate.now());
        grid.add(dpNgaySX, 1, row++);

        grid.add(new Label("Hạn SD:"), 0, row);
        dpHanSD = new DatePicker(LocalDate.now().plusYears(1));
        grid.add(dpHanSD, 1, row++);

        grid.add(new Label("Khối Lượng (*):"), 0, row);
        txtKhoiLuong = new TextField("0");
        grid.add(txtKhoiLuong, 1, row++);

        grid.add(new Label("Nước SX (*):"), 0, row);
        txtNuocSX = new TextField();
        grid.add(txtNuocSX, 1, row++);

        grid.add(new Label("Thương Hiệu (*):"), 0, row);
        txtThuongHieu = new TextField();
        grid.add(txtThuongHieu, 1, row++);

        grid.add(new Label("Giá Bán (*):"), 0, row);
        txtGiaBan = new TextField("0");
        grid.add(txtGiaBan, 1, row++);

        grid.add(new Label("Ngưỡng tồn tối thiểu:"), 0, row);
        txtNguongTon = new TextField("10");
        grid.add(txtNguongTon, 1, row++);

        grid.add(new Label("Tình Trạng:"), 0, row);
        cbTinhTrang = new ComboBox<>(FXCollections.observableArrayList(ETinhTrangSP.values()));
        grid.add(cbTinhTrang, 1, row++);

        grid.add(new Label("Loại SP:"), 0, row);
        cbLoaiSP = new ComboBox<>(FXCollections.observableArrayList(ELoaiSanPham.values()));
        grid.add(cbLoaiSP, 1, row++);

        grid.add(new Label("Đơn Vị Tính:"), 0, row);
        cbDonViTinh = new ComboBox<>();
        grid.add(cbDonViTinh, 1, row++);

        grid.add(new Label("Nhóm Thuốc (nếu là thuốc):"), 0, row);
        cbNhomThuoc = new ComboBox<>();
        grid.add(cbNhomThuoc, 1, row++);

        grid.add(new Label("Tác Dụng Phụ:"), 0, row);
        txtTacDungPhu = new TextField();
        grid.add(txtTacDungPhu, 1, row++);

        grid.add(new Label("Lời Khuyên:"), 0, row);
        txtLoiKhuyen = new TextField();
        grid.add(txtLoiKhuyen, 1, row++);

        grid.add(new Label("Công Dụng:"), 0, row);
        txtCongDung = new TextField();
        grid.add(txtCongDung, 1, row++);

        grid.add(new Label("Mô Tả:"), 0, row);
        txtMoTa = new TextArea();
        txtMoTa.setPrefRowCount(3);
        grid.add(txtMoTa, 1, row++);

        grid.add(new Label("Thuốc Kê Đơn:"), 0, row);
        chkThuocKeDon = new CheckBox();
        grid.add(chkThuocKeDon, 1, row++);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);

        // Buttons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(15));
        
        Button btnSave = new Button("Lưu");
        btnSave.setOnAction(e -> saveSanPham());
        
        Button btnCancel = new Button("Hủy");
        btnCancel.setOnAction(e -> close());

        btnBox.getChildren().addAll(btnSave, btnCancel);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(scrollPane);
        mainPane.setBottom(btnBox);

        Scene scene = new Scene(mainPane);
        setScene(scene);
    }

    private void loadComboData() {
        try {
            // Load DonViTinh
            List<DonViTinh> dvtList = RMICLientFactory.getDonViTinhService().getAll();
            cbDonViTinh.setItems(FXCollections.observableArrayList(dvtList));
            
            // Customize display
            cbDonViTinh.setCellFactory(lv -> new ListCell<DonViTinh>() {
                @Override
                protected void updateItem(DonViTinh item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTenDonViTinh());
                }
            });
            cbDonViTinh.setButtonCell(new ListCell<DonViTinh>() {
                @Override
                protected void updateItem(DonViTinh item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getTenDonViTinh());
                }
            });

            // Load NhomThuoc
            List<NhomThuoc> ntList = RMICLientFactory.getNhomThuocService().getAll();
            cbNhomThuoc.setItems(FXCollections.observableArrayList(ntList));
            cbNhomThuoc.getItems().add(0, null); // Add null option for non-drugs
            
            cbNhomThuoc.setCellFactory(lv -> new ListCell<NhomThuoc>() {
                @Override
                protected void updateItem(NhomThuoc item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else if (item == null) {
                        setText("-- Không có --");
                    } else {
                        setText(item.getTenNhomThuoc());
                    }
                }
            });
            cbNhomThuoc.setButtonCell(new ListCell<NhomThuoc>() {
                @Override
                protected void updateItem(NhomThuoc item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else if (item == null) {
                        setText("-- Không có --");
                    } else {
                        setText(item.getTenNhomThuoc());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi tải dữ liệu danh mục: " + e.getMessage());
        }
    }

    private void fillData(SanPham sp) {
        txtTenSP.setText(sp.getTenSP());
        dpNgaySX.setValue(sp.getNgaySX());
        dpHanSD.setValue(sp.getHanSD());
        txtKhoiLuong.setText(String.valueOf(sp.getKhoiLuong()));
        txtNuocSX.setText(sp.getNuocSX());
        txtThuongHieu.setText(sp.getThuongHieu());
        txtGiaBan.setText(String.valueOf(sp.getGiaBan()));
        txtNguongTon.setText(String.valueOf(sp.getNguongTonToiThieu()));
        cbTinhTrang.setValue(sp.getTinhTrangSP());
        cbLoaiSP.setValue(sp.getLoaiSP());
        txtMoTa.setText(sp.getMoTa());
        
        if (sp.getDonViTinh() != null) {
            for (DonViTinh dvt : cbDonViTinh.getItems()) {
                if (dvt != null && dvt.getMaDonViTinh().equals(sp.getDonViTinh().getMaDonViTinh())) {
                    cbDonViTinh.setValue(dvt);
                    break;
                }
            }
        }

        if (sp.getLoaiSP() == ELoaiSanPham.THUOC) {
            txtTacDungPhu.setText(sp.getTacDungPhu());
            chkThuocKeDon.setSelected(sp.isThuocKeDon());
            if (sp.getNhomThuoc() != null) {
                for (NhomThuoc nt : cbNhomThuoc.getItems()) {
                    if (nt != null && nt.getMaNhomThuoc().equals(sp.getNhomThuoc().getMaNhomThuoc())) {
                        cbNhomThuoc.setValue(nt);
                        break;
                    }
                }
            }
        }
        txtTacDungPhu.setText(sp.getTacDungPhu());
        // Always show these values if present, regardless of product type.
        txtLoiKhuyen.setText(sp.getLoiKhuyen());
        txtCongDung.setText(sp.getCongDung());
    }

    private String getSafeText(TextInputControl control) {
        if (control == null || control.getText() == null) return "";
        return control.getText().trim();
    }

    private void saveSanPham() {
        try {
            if (getSafeText(txtTenSP).isEmpty() || getSafeText(txtNuocSX).isEmpty() || getSafeText(txtThuongHieu).isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ các trường bắt buộc (*)");
                return;
            }

            SanPham sp = (currentSanPham == null) ? new SanPham() : currentSanPham;

            sp.setTenSP(getSafeText(txtTenSP));
            if (dpNgaySX.getValue() == null || dpHanSD.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đầy đủ Ngày SX và Hạn SD.");
                return;
            }
            if (dpHanSD.getValue().isBefore(dpNgaySX.getValue())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Hạn SD phải lớn hơn hoặc bằng Ngày SX.");
                return;
            }

            sp.setNgaySX(dpNgaySX.getValue());
            sp.setHanSD(dpHanSD.getValue());
            
            String khoiLuongText = getSafeText(txtKhoiLuong);
            sp.setKhoiLuong(khoiLuongText.isEmpty() ? 0 : Double.parseDouble(khoiLuongText));
            
            sp.setNuocSX(getSafeText(txtNuocSX));
            sp.setThuongHieu(getSafeText(txtThuongHieu));
            
            String giaBanText = getSafeText(txtGiaBan);
            sp.setGiaBan(giaBanText.isEmpty() ? 0 : Double.parseDouble(giaBanText));

            String nguongTonText = getSafeText(txtNguongTon);
            int nguongTon = nguongTonText.isEmpty() ? 10 : Integer.parseInt(nguongTonText);
            if (nguongTon < 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngưỡng tồn tối thiểu không được âm.");
                return;
            }
            sp.setNguongTonToiThieu(nguongTon);
            
            sp.setTinhTrangSP(cbTinhTrang.getValue());
            sp.setLoaiSP(cbLoaiSP.getValue());
            sp.setMoTa(getSafeText(txtMoTa));
            sp.setDonViTinh(cbDonViTinh.getValue());
            
            sp.setTacDungPhu(getSafeText(txtTacDungPhu));
            sp.setLoiKhuyen(getSafeText(txtLoiKhuyen));
            sp.setCongDung(getSafeText(txtCongDung));

            if (sp.getLoaiSP() == ELoaiSanPham.THUOC) {
                sp.setThuocKeDon(chkThuocKeDon.isSelected());
                sp.setNhomThuoc(cbNhomThuoc.getValue());
            } else {
                sp.setThuocKeDon(false);
                sp.setNhomThuoc(null);
            }

            SanPhamService sanPhamService = RMICLientFactory.getSanPhamService();
            boolean success = false;
            
            if (currentSanPham == null) {
                success = sanPhamService.save(sp);
            } else {
                success = sanPhamService.update(sp);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Lưu sản phẩm thành công!");
                dashboard.loadData();
                close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lưu sản phẩm thất bại. Vui lòng kiểm tra lại.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Lỗi dữ liệu: " + ex.getMessage());
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
