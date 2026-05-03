package client.ui;

import client.RMICLientFactory;
import entities.ChiTietPhieuXuat;
import entities.PhieuXuat;
import entities.SanPham;
import entities.NhanVien;
import entities.PhieuNhap;
import entities.ChiTietPhieuNhap;
import entities.enums.VaiTro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ExportDialog extends Stage {
    private TableView<ChiTietPhieuXuat> table;
    private ObservableList<ChiTietPhieuXuat> exportList = FXCollections.observableArrayList();
    private TextField txtLyDo = new TextField();
    private NhanVien nhanVien;
    private ComboBox<String> cbChiNhanh = new ComboBox<>();
    private ComboBox<String> cbChiNhanhNhan = new ComboBox<>();
    private ListView<SanPham> lvProducts;
    private TextField txtSearch;
    private static final String KHO_TONG_LABEL = "Kho tổng (Admin)";

    public ExportDialog(NhanVien nv) {
        this.nhanVien = nv;
        setTitle("Lập Phiếu Xuất Kho (Điều chỉnh/Hủy hàng)");
        initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        GridPane topGrid = new GridPane();
        topGrid.setHgap(10); topGrid.setVgap(10);
        topGrid.add(new Label("Nhân viên:"), 0, 0); topGrid.add(new Label(nv.getHoTen()), 1, 0);
        topGrid.add(new Label("Lý do xuất:"), 0, 1); topGrid.add(txtLyDo, 1, 1);
        topGrid.add(new Label("Chi nhánh xuất:"), 0, 2); topGrid.add(cbChiNhanh, 1, 2);
        topGrid.add(new Label("Chi nhánh nhận:"), 0, 3); topGrid.add(cbChiNhanhNhan, 1, 3);
        root.setTop(topGrid);

        table = new TableView<>();
        TableColumn<ChiTietPhieuXuat, String> colTen = new TableColumn<>("Tên Sản Phẩm");
        colTen.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(() -> data.getValue().getSanPham().getTenSP()));
        TableColumn<ChiTietPhieuXuat, Integer> colSoLuong = new TableColumn<>("Số Lượng Xuất");
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        table.getColumns().addAll(colTen, colSoLuong);
        table.setItems(exportList);
        root.setCenter(table);

        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(0, 0, 0, 15));
        rightBox.setPrefWidth(250);
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm thuốc cần xuất...");
        lvProducts = new ListView<>();
        initBranchSelector();
        cbChiNhanh.setOnAction(e -> refreshProductsByBranch());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshProductsByBranch();
        });
        
        Button btnAdd = new Button("➕ Thêm vào phiếu");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> {
            SanPham selected = lvProducts.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog tid = new TextInputDialog("1");
                tid.setTitle("Số lượng");
                tid.setHeaderText("Xuất bao nhiêu từ tồn kho? (Hiện có: " + selected.getSoLuongTon() + ")");
                tid.showAndWait().ifPresent(v -> {
                    ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                    ct.setSanPham(selected);
                    ct.setSoLuong(Integer.parseInt(v));
                    exportList.add(ct);
                });
            }
        });
        rightBox.getChildren().addAll(txtSearch, lvProducts, btnAdd);
        root.setRight(rightBox);

        Button btnSave = new Button("💾 Hoàn Tất Xuất Kho");
        btnSave.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> saveExport());
        HBox bottom = new HBox(btnSave); bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(15, 0, 0, 0));
        root.setBottom(bottom);

        setScene(new Scene(root, 800, 500));
        refreshProductsByBranch();
    }

    private void saveExport() {
        if (exportList.isEmpty()) return;
        PhieuXuat px = new PhieuXuat();
        px.setNgayXuat(LocalDate.now());
        px.setLyDoXuat(txtLyDo.getText());
        px.setNhanVien(nhanVien);
        if (isAdmin()) {
            px.setChiNhanh(null);
        } else {
            px.setChiNhanh(cbChiNhanh.getValue());
        }
        px.setChiTietPhieuXuats(new ArrayList<>(exportList));
        try {
            boolean success = RMICLientFactory.getPhieuXuatService().save(px);
            if (!success) {
                new Alert(Alert.AlertType.ERROR, "Lỗi khi lưu phiếu xuất! (Kiểm tra tồn kho)").show();
                return;
            }

            // Admin điều chuyển từ kho tổng sang chi nhánh nhận
            if (isAdmin()) {
                String branchTarget = cbChiNhanhNhan.getValue();
                if (branchTarget == null || branchTarget.isBlank() || KHO_TONG_LABEL.equals(branchTarget)) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng chọn chi nhánh nhận.").show();
                    return;
                }
                boolean imported = createTransferImport(branchTarget);
                if (!imported) {
                    new Alert(Alert.AlertType.ERROR,
                            "Đã xuất khỏi kho tổng nhưng tạo phiếu nhập cho chi nhánh nhận thất bại. Vui lòng kiểm tra lại.")
                            .show();
                    return;
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Xuất kho thành công! Kho đã được cập nhật.").show();
            close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initBranchSelector() {
        String ownBranch = nhanVien != null ? nhanVien.getChiNhanh() : null;
        if (isAdmin()) {
            cbChiNhanh.getItems().setAll(KHO_TONG_LABEL);
            cbChiNhanh.setValue(KHO_TONG_LABEL);
            cbChiNhanh.setDisable(true);
            loadTargetBranchesForAdmin();
            return;
        }

        try {
            Set<String> branches = new TreeSet<>();
            List<NhanVien> allNvs = RMICLientFactory.getEmployeeService().getAll();
            for (NhanVien nv : allNvs) {
                if (nv != null && nv.getChiNhanh() != null && !nv.getChiNhanh().isBlank()) {
                    branches.add(nv.getChiNhanh());
                }
            }
            cbChiNhanh.getItems().setAll(branches);
        } catch (Exception e) {
            cbChiNhanh.getItems().clear();
        }

        if (ownBranch != null && !ownBranch.isBlank() && !cbChiNhanh.getItems().contains(ownBranch)) {
            cbChiNhanh.getItems().add(ownBranch);
        }
        cbChiNhanh.setValue(ownBranch);
        cbChiNhanh.setDisable(true);
        cbChiNhanhNhan.getItems().setAll("Không áp dụng");
        cbChiNhanhNhan.setValue("Không áp dụng");
        cbChiNhanhNhan.setDisable(true);
    }

    private void refreshProductsByBranch() {
        try {
            if (isAdmin()) {
                ObservableList<SanPham> all = FXCollections.observableArrayList(
                        RMICLientFactory.getSanPhamService().getAll()
                );
                String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
                if (!keyword.isEmpty()) {
                    all = all.filtered(p -> p.getTenSP() != null && p.getTenSP().toLowerCase().contains(keyword));
                }
                lvProducts.setItems(all);
                return;
            }
            String branch = cbChiNhanh.getValue();
            if (branch == null || branch.isBlank()) {
                lvProducts.getItems().clear();
                return;
            }
            ObservableList<SanPham> all = FXCollections.observableArrayList(
                    RMICLientFactory.getSanPhamService().getAllByChiNhanh(branch)
            );
            String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
            if (!keyword.isEmpty()) {
                all = all.filtered(p -> p.getTenSP() != null && p.getTenSP().toLowerCase().contains(keyword));
            }
            lvProducts.setItems(all);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAdmin() {
        return nhanVien != null
                && nhanVien.getTaiKhoan() != null
                && nhanVien.getTaiKhoan().getVaiTro() == entities.enums.VaiTro.ADMIN;
    }

    private void loadTargetBranchesForAdmin() {
        try {
            Set<String> branches = new TreeSet<>();
            List<NhanVien> allNvs = RMICLientFactory.getEmployeeService().getAll();
            for (NhanVien nv : allNvs) {
                if (nv != null && nv.getChiNhanh() != null && !nv.getChiNhanh().isBlank()) {
                    branches.add(nv.getChiNhanh());
                }
            }
            cbChiNhanhNhan.getItems().setAll(branches);
            cbChiNhanhNhan.setDisable(false);
            if (!cbChiNhanhNhan.getItems().isEmpty()) {
                cbChiNhanhNhan.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            cbChiNhanhNhan.getItems().clear();
            cbChiNhanhNhan.setDisable(false);
        }
    }

    private boolean createTransferImport(String targetBranch) {
        try {
            List<NhanVien> allNvs = RMICLientFactory.getEmployeeService().getAll();
            NhanVien manager = allNvs.stream()
                    .filter(nv -> nv != null
                            && nv.getChiNhanh() != null
                            && targetBranch.trim().equalsIgnoreCase(nv.getChiNhanh().trim())
                            && nv.getTaiKhoan() != null
                            && nv.getTaiKhoan().getVaiTro() == VaiTro.QUANLY)
                    .findFirst()
                    .orElse(null);
            
            if (manager == null) {
                // Try finding any employee in that branch if no manager is found
                manager = allNvs.stream()
                        .filter(nv -> nv != null
                                && nv.getChiNhanh() != null
                                && targetBranch.trim().equalsIgnoreCase(nv.getChiNhanh().trim()))
                        .findFirst()
                        .orElse(null);
            }

            if (manager == null) {
                return false;
            }

            PhieuNhap pn = new PhieuNhap();
            pn.setNgayNhap(LocalDate.now());
            pn.setNhanVien(manager);
            pn.setChiNhanh(targetBranch);
            pn.setNhaCungCap("Điều chuyển nội bộ từ kho tổng");

            List<ChiTietPhieuNhap> details = new ArrayList<>();
            double total = 0;
            for (ChiTietPhieuXuat x : exportList) {
                ChiTietPhieuNhap in = new ChiTietPhieuNhap();
                in.setPhieuNhap(pn);
                in.setSanPham(x.getSanPham());
                in.setSoLuong(x.getSoLuong());
                double giaNhap = x.getSanPham() != null && x.getSanPham().getGiaBan() > 0
                        ? x.getSanPham().getGiaBan() * 0.65
                        : 0;
                in.setGiaNhap(giaNhap);
                in.setNgayHetHan(x.getSanPham() != null ? x.getSanPham().getHanSD() : null);
                details.add(in);
                total += giaNhap * in.getSoLuong();
            }
            pn.setChiTietPhieuNhaps(details);
            pn.setTongTien(total);

            return RMICLientFactory.getPhieuNhapService().save(pn);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
