package client.ui;

import client.RMICLientFactory;
import entities.ChiTietPhieuNhap;
import entities.PhieuNhap;
import entities.SanPham;
import entities.NhanVien;
import javafx.application.Platform;
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

public class ImportDialog extends Stage {
    private TableView<ChiTietPhieuNhap> table;
    private ObservableList<ChiTietPhieuNhap> importList = FXCollections.observableArrayList();
    private TextField txtNCC = new TextField();
    private Label lblTotal = new Label("Tổng tiền: 0 VNĐ");
    private NhanVien nhanVien;
    private MainDashboard dashboard;
    private ComboBox<String> cbChiNhanh = new ComboBox<>();
    private ListView<SanPham> lvProducts;
    private TextField txtSearch;
    private static final String KHO_TONG_LABEL = "Kho tổng (Admin)";

    public ImportDialog(MainDashboard dashboard, NhanVien nv) {
        this.dashboard = dashboard;
        this.nhanVien = nv;
        setTitle("Lập Phiếu Nhập Hàng");
        initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top: Info
        GridPane topGrid = new GridPane();
        topGrid.setHgap(10);
        topGrid.setVgap(10);
        topGrid.add(new Label("Nhân viên:"), 0, 0);
        topGrid.add(new Label(nv.getHoTen()), 1, 0);
        topGrid.add(new Label("Nhà cung cấp:"), 0, 1);
        topGrid.add(txtNCC, 1, 1);
        topGrid.add(new Label("Chi nhánh nhập:"), 0, 2);
        topGrid.add(cbChiNhanh, 1, 2);
        root.setTop(topGrid);

        // Center: Table
        table = new TableView<>();
        TableColumn<ChiTietPhieuNhap, String> colTen = new TableColumn<>("Tên Sản Phẩm");
        colTen.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(() -> data.getValue().getSanPham().getTenSP()));
        
        TableColumn<ChiTietPhieuNhap, Integer> colSoLuong = new TableColumn<>("Số Lượng");
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        
        TableColumn<ChiTietPhieuNhap, Double> colGia = new TableColumn<>("Giá Nhập");
        colGia.setCellValueFactory(new PropertyValueFactory<>("giaNhap"));

        TableColumn<ChiTietPhieuNhap, LocalDate> colHSD = new TableColumn<>("Hạn Sử Dụng");
        colHSD.setCellValueFactory(new PropertyValueFactory<>("ngayHetHan"));

        table.getColumns().addAll(colTen, colSoLuong, colGia, colHSD);
        table.setItems(importList);
        root.setCenter(table);

        // Right: Product Selector
        VBox rightBox = new VBox(10);
        rightBox.setPadding(new Insets(0, 0, 0, 15));
        rightBox.setPrefWidth(250);

        Label lblSearch = new Label("🔍 Tìm thuốc để nhập:");
        txtSearch = new TextField();
        lvProducts = new ListView<>();
        initBranchSelector();
        cbChiNhanh.setOnAction(e -> refreshProductsByBranch());
        
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshProductsByBranch();
        });

        lvProducts.setCellFactory(param -> new ListCell<SanPham>() {
            @Override
            protected void updateItem(SanPham item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getTenSP() + " (Tồn: " + item.getSoLuongTon() + ")");
            }
        });

        Button btnAddToList = new Button("➕ Thêm vào phiếu");
        btnAddToList.setMaxWidth(Double.MAX_VALUE);
        btnAddToList.setOnAction(e -> {
            SanPham selected = lvProducts.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showQuantityPriceDialog(selected);
            }
        });

        Button btnAddNewProduct = new Button("✨ Thêm thuốc mới");
        btnAddNewProduct.setMaxWidth(Double.MAX_VALUE);
        btnAddNewProduct.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnAddNewProduct.setOnAction(e -> {
            ProductDialog dialog = new ProductDialog(dashboard, null);
            dialog.showAndWait();
        });

        rightBox.getChildren().addAll(lblSearch, txtSearch, lvProducts, btnAddToList, btnAddNewProduct);
        root.setRight(rightBox);

        // Bottom: Footer
        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(15, 0, 0, 0));
        
        Button btnSave = new Button("💾 Hoàn Tất Nhập Hàng");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnSave.setOnAction(e -> saveImport());

        bottom.getChildren().addAll(lblTotal, btnSave);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 900, 600);
        setScene(scene);
        Platform.runLater(this::refreshProductsByBranch);
    }

    private void showQuantityPriceDialog(SanPham sp) {
        Dialog<ChiTietPhieuNhap> dialog = new Dialog<>();
        dialog.setTitle("Nhập số lượng & giá");
        ButtonType okButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField txtQty = new TextField("10");
        TextField txtPrice = new TextField(String.valueOf(sp.getGiaBan() * 0.7)); // Gợi ý giá nhập = 70% giá bán
        DatePicker dpExpiry = new DatePicker(LocalDate.now().plusYears(2)); // Mặc định 2 năm sau

        grid.add(new Label("Sản phẩm:"), 0, 0); grid.add(new Label(sp.getTenSP()), 1, 0);
        grid.add(new Label("Số lượng nhập:"), 0, 1); grid.add(txtQty, 1, 1);
        grid.add(new Label("Giá nhập mỗi đv:"), 0, 2); grid.add(txtPrice, 1, 2);
        grid.add(new Label("Hạn sử dụng:"), 0, 3); grid.add(dpExpiry, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> {
            if (b == okButtonType) {
                ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                ct.setSanPham(sp);
                ct.setSoLuong(Integer.parseInt(txtQty.getText()));
                ct.setGiaNhap(Double.parseDouble(txtPrice.getText()));
                ct.setNgayHetHan(dpExpiry.getValue());
                return ct;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(ct -> {
            importList.add(ct);
            updateTotal();
        });
    }

    private void updateTotal() {
        double total = importList.stream().mapToDouble(c -> c.getGiaNhap() * c.getSoLuong()).sum();
        lblTotal.setText(String.format("Tổng tiền: %,.0f VNĐ", total));
    }

    private void saveImport() {
        if (importList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng thêm sản phẩm vào phiếu!").show();
            return;
        }
        PhieuNhap pn = new PhieuNhap();
        pn.setNgayNhap(LocalDate.now());
        pn.setNhaCungCap(txtNCC.getText());
        pn.setNhanVien(nhanVien);
        if (isAdmin()) {
            pn.setChiNhanh(null);
        } else {
            pn.setChiNhanh(cbChiNhanh.getValue());
        }
        pn.setChiTietPhieuNhaps(new ArrayList<>(importList));

        try {
            boolean success = RMICLientFactory.getPhieuNhapService().save(pn);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Nhập hàng thành công! Kho đã được cập nhật.").show();
                close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Lỗi khi lưu phiếu nhập!").show();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initBranchSelector() {
        String ownBranch = nhanVien != null ? nhanVien.getChiNhanh() : null;
        if (isAdmin()) {
            cbChiNhanh.getItems().setAll(KHO_TONG_LABEL);
            cbChiNhanh.setValue(KHO_TONG_LABEL);
            cbChiNhanh.setDisable(true);
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
}
