package client.ui;

import client.RMICLientFactory;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import entities.*;
import entities.enums.ETinhTrangSP;
import entities.enums.VaiTro;
import service.HoaDonService;
import service.KhachHangService;
import service.SanPhamService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Side;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.chart.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MainDashboard extends Application {

    private TableView<SanPham> productTable;
    private ObservableList<SanPham> productList;
    private FilteredList<SanPham> filteredProductList;
    private ComboBox<String> cbProductBranch;
    private String currentProductViewBranch;
    private Label lblLoginStatus;
    private TaiKhoan loggedInAccount;

    // POS Fields
    private TableView<SanPham> posProductTable;
    private TableView<CartItem> cartTable;
    private ObservableList<CartItem> cartList;
    private TextField txtSearchPhone;
    private Label lblCusName, lblCusPoints;
    private CheckBox chkUsePoints;
    private Label lblTotal, lblVAT, lblDiscount, lblFinalTotal;
    private KhachHang currentCustomer;

    // Customer Management fields
    private TableView<KhachHang> customerTable;
    private ObservableList<KhachHang> customerList;
    private FilteredList<KhachHang> filteredCustomerList;

    // Employee Management fields
    private TableView<NhanVien> employeeTable;
    private ObservableList<NhanVien> employeeList;
    private FilteredList<NhanVien> filteredEmployeeList;
    private java.util.Map<String, Double> employeeRevenueMap = new java.util.HashMap<>();

    // Sidebar Buttons
    private Button btnEmployee;
    private Button btnImport;
    private Button btnExport;
    private Button btnStatistics;
    
    // Auth Button
    private Button btnLogin;

    private BorderPane statisticsPane;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pharmacy Management System");

        BorderPane mainPane = new BorderPane();

        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("-fx-background-color: #2980b9;");

        Label titleLabel = new Label("Pharmacy System Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblLoginStatus = new Label("Chưa đăng nhập");
        lblLoginStatus.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        btnLogin = new Button("Đăng nhập");
        btnLogin.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnLogin.setOnAction(e -> {
            if (loggedInAccount != null) {
                setLoggedInAccount(null);
                updateRolePermissions();
            } else {
                showLoginDialog();
            }
        });

        Button btnChangePass = new Button("Đổi mật khẩu");
        btnChangePass.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnChangePass.setOnAction(e -> showChangePasswordDialog());

        headerBox.getChildren().addAll(titleLabel, spacer, lblLoginStatus, btnChangePass, btnLogin);
        mainPane.setTop(headerBox);

        // Center Content Area
        StackPane contentArea = new StackPane();
        BorderPane productPane = createProductManagementPane();
        SplitPane posPane = createPOSPane();
        BorderPane customerPane = createCustomerManagementPane();
        BorderPane invoicePane = createInvoiceManagementPane();
        BorderPane employeePane = createEmployeeManagementPane();

        // Left Sidebar
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPrefWidth(240);

        Button btnPOS = new Button("🛒 Bán Hàng (POS)");
        Button btnProduct = new Button("📦 Quản Lý Sản Phẩm");
        Button btnCustomer = new Button("👥 Quản Lý Khách Hàng");
        Button btnInvoice = new Button("🧾 Quản Lý Hóa Đơn");
        btnEmployee = new Button("🛡️ Quản Lý Nhân Viên (Admin)");
        btnImport = new Button("🚚 Nhập Hàng (Admin)");
        btnExport = new Button("📦 Xuất Kho (Admin)");
        btnStatistics = new Button("📊 Thống Kê (Admin)");

        Button[] navButtons = {btnPOS, btnProduct, btnCustomer, btnInvoice, btnEmployee, btnImport, btnExport, btnStatistics};
        String activeStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 12 15;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 12 15;";

        for (Button b : navButtons) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle(inactiveStyle);
        }
        btnPOS.setStyle(activeStyle);

        btnPOS.setOnAction(e -> { contentArea.getChildren().setAll(posPane); setActiveButton(btnPOS, navButtons, activeStyle, inactiveStyle); });
        btnProduct.setOnAction(e -> { contentArea.getChildren().setAll(productPane); setActiveButton(btnProduct, navButtons, activeStyle, inactiveStyle); });
        btnCustomer.setOnAction(e -> { contentArea.getChildren().setAll(customerPane); setActiveButton(btnCustomer, navButtons, activeStyle, inactiveStyle); });
        btnInvoice.setOnAction(e -> { contentArea.getChildren().setAll(invoicePane); setActiveButton(btnInvoice, navButtons, activeStyle, inactiveStyle); });
        btnEmployee.setOnAction(e -> { contentArea.getChildren().setAll(employeePane); setActiveButton(btnEmployee, navButtons, activeStyle, inactiveStyle); });
        btnImport.setOnAction(e -> { contentArea.getChildren().setAll(createImportManagementPane()); setActiveButton(btnImport, navButtons, activeStyle, inactiveStyle); });
        btnExport.setOnAction(e -> { contentArea.getChildren().setAll(createExportManagementPane()); setActiveButton(btnExport, navButtons, activeStyle, inactiveStyle); });
        btnStatistics.setOnAction(e -> { 
            if (statisticsPane == null) statisticsPane = createStatisticsPane();
            contentArea.getChildren().setAll(statisticsPane); 
            setActiveButton(btnStatistics, navButtons, activeStyle, inactiveStyle); 
        });

        sidebar.getChildren().addAll(btnPOS, btnProduct, btnCustomer, btnInvoice, btnEmployee, btnImport, btnExport, btnStatistics);
        
        mainPane.setLeft(sidebar);
        mainPane.setCenter(contentArea);

        contentArea.getChildren().setAll(posPane);
        updateRolePermissions();

        Scene scene = new Scene(mainPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.runLater(primaryStage::requestFocus);

        loadDataAsync();
    }

    private <T> void formatCurrencyColumn(TableColumn<T, Double> column) {
        column.setCellFactory(tc -> new TableCell<T, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", price));
                }
            }
        });
    }

    private void setActiveButton(Button active, Button[] all, String activeStyle, String inactiveStyle) {
        for (Button b : all) {
            b.setStyle(inactiveStyle);
        }
        active.setStyle(activeStyle);
    }

    private void updateRolePermissions() {
        if (loggedInAccount == null) return;
        boolean isAdmin = loggedInAccount.getVaiTro() == VaiTro.ADMIN;
        boolean isManager = loggedInAccount.getVaiTro() == VaiTro.QUANLY;
        
        if (btnEmployee != null) {
            btnEmployee.setVisible(isAdmin || isManager);
            btnEmployee.setManaged(isAdmin || isManager);
            if (isManager && loggedInAccount.getNhanVien() != null) {
                String branch = loggedInAccount.getNhanVien().getChiNhanh();
                if (branch == null || branch.isBlank()) {
                    branch = "Chưa gán chi nhánh";
                }
                btnEmployee.setText("🛡️ Quản Lý NV - " + branch);
            } else {
                btnEmployee.setText("🛡️ Quản Lý Nhân Viên (Admin)");
            }
        }
        
        if (btnStatistics != null) {
            btnStatistics.setVisible(isAdmin || isManager);
            btnStatistics.setManaged(isAdmin || isManager);
        }
        
        if (btnImport != null) {
            btnImport.setVisible(isAdmin || isManager);
            btnImport.setManaged(isAdmin || isManager);
        }
        
        if (btnExport != null) {
            btnExport.setVisible(isAdmin || isManager);
            btnExport.setManaged(isAdmin || isManager);
        }
    }

    public void setLoggedInAccount(TaiKhoan tk) {
        this.loggedInAccount = tk;
        if (lblLoginStatus != null) {
            if (tk != null) {
                if (tk.getNhanVien() != null && tk.getNhanVien().getHoTen() != null) {
                    lblLoginStatus.setText("Xin chào: " + tk.getNhanVien().getHoTen() + " (" + tk.getVaiTro() + ")");
                } else {
                    lblLoginStatus.setText("Xin chào: " + tk.getMaTaiKhoan() + " (" + tk.getVaiTro() + ")");
                }
                if (btnLogin != null) {
                    btnLogin.setText("Đăng xuất");
                    btnLogin.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                }
            } else {
                lblLoginStatus.setText("Chưa đăng nhập");
                if (btnLogin != null) {
                    btnLogin.setText("Đăng nhập");
                    btnLogin.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
                }
            }
        }
        updateRolePermissions();
        initProductBranchFilter();
        if (productList != null) {
            loadDataAsync();
        }
        if (employeeList != null) {
            refreshEmployeeList();
        }
    }

    public TaiKhoan getLoggedInAccount() {
        return loggedInAccount;
    }

    private BorderPane createCustomerManagementPane() {
        BorderPane pane = new BorderPane();
        customerTable = new TableView<>();
        customerList = FXCollections.observableArrayList();
        filteredCustomerList = new FilteredList<>(customerList, p -> true);
        
        // Wrap FilteredList in a SortedList
        javafx.collections.transformation.SortedList<KhachHang> sortedCustomerList = new javafx.collections.transformation.SortedList<>(filteredCustomerList);
        sortedCustomerList.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedCustomerList);

        TableColumn<KhachHang, String> colHoTen = new TableColumn<>("Họ Tên"); colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen")); colHoTen.setPrefWidth(200);
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số Điện Thoại"); colSdt.setCellValueFactory(new PropertyValueFactory<>("soDienThoai")); colSdt.setPrefWidth(150);
        
        TableColumn<KhachHang, Double> colDiem = new TableColumn<>("Điểm Tích Lũy"); 
        colDiem.setCellValueFactory(new PropertyValueFactory<>("diemTichLuy")); 
        colDiem.setPrefWidth(150);
        colDiem.setCellFactory(tc -> new TableCell<KhachHang, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Hyperlink link = new Hyperlink(String.format("%,.0f", item));
                    link.setOnAction(e -> {
                        KhachHang kh = getTableView().getItems().get(getIndex());
                        if (kh != null) showCustomerHistory(kh);
                    });
                    setGraphic(link);
                }
            }
        });
        TableColumn<KhachHang, LocalDate> colNgayTG = new TableColumn<>("Ngày Tham Gia"); colNgayTG.setCellValueFactory(new PropertyValueFactory<>("ngayThamGia")); colNgayTG.setPrefWidth(150);

        customerTable.getColumns().addAll(colHoTen, colSdt, colDiem, colNgayTG);

        // Top Box: Search & Sort
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(10, 10, 0, 10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo tên hoặc SĐT...");
        txtSearch.setPrefWidth(250);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredCustomerList.setPredicate(kh -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return kh.getHoTen().toLowerCase().contains(lowerCaseFilter) 
                        || kh.getSoDienThoai().contains(lowerCaseFilter);
            });
        });

        Label lblSort = new Label("Sắp xếp theo:");
        ComboBox<String> cbSort = new ComboBox<>();
        cbSort.getItems().addAll("Tên (A-Z)", "Điểm (Giảm dần)", "Ngày tham gia (Mới nhất)");
        cbSort.setOnAction(e -> {
            String sortType = cbSort.getValue();
            if ("Tên (A-Z)".equals(sortType)) {
                customerTable.getSortOrder().setAll(colHoTen);
                colHoTen.setSortType(TableColumn.SortType.ASCENDING);
            } else if ("Điểm (Giảm dần)".equals(sortType)) {
                customerTable.getSortOrder().setAll(colDiem);
                colDiem.setSortType(TableColumn.SortType.DESCENDING);
            } else if ("Ngày tham gia (Mới nhất)".equals(sortType)) {
                customerTable.getSortOrder().setAll(colNgayTG);
                colNgayTG.setSortType(TableColumn.SortType.DESCENDING);
            }
        });

        topBox.getChildren().addAll(new Label("Tìm kiếm:"), txtSearch, lblSort, cbSort);
        pane.setTop(topBox);
        
        VBox centerBox = new VBox(customerTable);
        centerBox.setPadding(new Insets(10));
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        pane.setCenter(centerBox);

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setOnAction(e -> refreshCustomerList());

        Button btnAdd = new Button("Thêm");
        btnAdd.setOnAction(e -> {
            CustomerDialog dialog = new CustomerDialog(this, null);
            dialog.showAndWait();
        });

        Button btnEdit = new Button("Sửa");
        btnEdit.setOnAction(e -> editCustomer());

        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteCustomer());

        bottomBox.getChildren().addAll(btnRefresh, btnAdd, btnEdit, btnDelete);
        pane.setBottom(bottomBox);

        refreshCustomerList();
        return pane;
    }

    public void refreshCustomerList() {
        CompletableFuture.supplyAsync(() -> {
            try { 
                return RMICLientFactory.getKhachHangService().getAll(); 
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                return null; 
            }
        }).thenAccept(data -> { 
            if (data != null) Platform.runLater(() -> customerList.setAll(data)); 
        });
    }

    private void editCustomer() {
        KhachHang selectedItem = customerTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        CustomerDialog dialog = new CustomerDialog(this, selectedItem);
        dialog.showAndWait();
    }

    private void deleteCustomer() {
        KhachHang selectedItem = customerTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Chắc chắn xóa khách hàng này?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    RMICLientFactory.getKhachHangService().delete(selectedItem.getMaKhachHang());
                    refreshCustomerList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi xóa", "Không thể xóa khách hàng: " + ex.getMessage());
                }
            }
        });
    }

    private void showCustomerHistory(KhachHang kh) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lịch Sử Mua Hàng: " + kh.getHoTen() + " - " + kh.getSoDienThoai());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<HoaDon> historyTable = new TableView<>();
        
        TableColumn<HoaDon, String> colMaHD = new TableColumn<>("Mã Hóa Đơn");
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colMaHD.setPrefWidth(250);
        
        TableColumn<HoaDon, LocalDate> colNgayHD = new TableColumn<>("Ngày Lập");
        colNgayHD.setCellValueFactory(new PropertyValueFactory<>("ngayLapHD"));
        colNgayHD.setPrefWidth(120);
        
        TableColumn<HoaDon, Double> colTongTien = new TableColumn<>("Tổng Tiền (VNĐ)");
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tienKhachHangPhaiThanhToan"));
        colTongTien.setPrefWidth(180);
        colTongTien.setCellFactory(tc -> new TableCell<HoaDon, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("%,.0f VNĐ", item));
            }
        });

        historyTable.getColumns().addAll(colMaHD, colNgayHD, colTongTien);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(new Label("Lịch sử giao dịch (100 VNĐ = 1 Điểm)"), historyTable);
        
        dialog.getDialogPane().setContent(vbox);
        dialog.setWidth(600);
        dialog.setHeight(400);

        CompletableFuture.supplyAsync(() -> {
            try { 
                List<HoaDon> allHD = RMICLientFactory.getHoaDonService().getAll(); 
                return allHD.stream()
                        .filter(hd -> hd.getKhachHang() != null && hd.getKhachHang().getMaKhachHang().equals(kh.getMaKhachHang()))
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception ex) { 
                return null; 
            }
        }).thenAccept(data -> { 
            if (data != null) Platform.runLater(() -> historyTable.getItems().setAll(data)); 
        });

        dialog.showAndWait();
    }

    private BorderPane createInvoiceManagementPane() {
        BorderPane pane = new BorderPane();
        TableView<HoaDon> table = new TableView<>();
        ObservableList<HoaDon> allInvoices = FXCollections.observableArrayList();
        FilteredList<HoaDon> filteredInvoices = new FilteredList<>(allInvoices, p -> true);
        table.setItems(filteredInvoices);

        TableColumn<HoaDon, String> colMaHD = new TableColumn<>("Mã Hóa Đơn"); colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHoaDon")); colMaHD.setPrefWidth(200);
        TableColumn<HoaDon, LocalDate> colNgay = new TableColumn<>("Ngày Lập"); colNgay.setCellValueFactory(new PropertyValueFactory<>("ngayLapHD")); colNgay.setPrefWidth(120);
        TableColumn<HoaDon, String> colNV = new TableColumn<>("Nhân Viên Lập"); colNV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNhanVien().getHoTen())); colNV.setPrefWidth(200);
        TableColumn<HoaDon, String> colChiNhanh = new TableColumn<>("Chi Nhánh Lập");
        colChiNhanh.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNhanVien() != null && c.getValue().getNhanVien().getChiNhanh() != null
                        ? c.getValue().getNhanVien().getChiNhanh()
                        : "Chưa gán"));
        colChiNhanh.setPrefWidth(180);
        TableColumn<HoaDon, String> colKH = new TableColumn<>("Khách Hàng"); colKH.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKhachHang() != null ? c.getValue().getKhachHang().getHoTen() : "Khách lẻ")); colKH.setPrefWidth(200);
        TableColumn<HoaDon, Double> colTien = new TableColumn<>("Tổng Tiền"); 
        colTien.setCellValueFactory(new PropertyValueFactory<>("tienKhachHangPhaiThanhToan")); 
        colTien.setPrefWidth(150);
        colTien.setCellFactory(tc -> new TableCell<HoaDon, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Hyperlink link = new Hyperlink(String.format("%,.0f VNĐ", item));
                    link.setOnAction(e -> {
                        HoaDon hd = getTableView().getItems().get(getIndex());
                        if (hd != null) showInvoiceDetailsDialog(hd);
                    });
                    setGraphic(link);
                }
            }
        });

        TableColumn<HoaDon, Void> colPrint = new TableColumn<>("In Hóa Đơn");
        colPrint.setPrefWidth(120);
        colPrint.setCellFactory(tc -> new TableCell<>() {
            private final Button btnPrint = new Button("In");
            {
                btnPrint.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold;");
                btnPrint.setOnAction(e -> {
                    HoaDon hd = getTableView().getItems().get(getIndex());
                    if (hd != null) {
                        previewInvoicePdf(hd);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnPrint);
            }
        });

        table.getColumns().addAll(colMaHD, colNgay, colNV, colChiNhanh, colKH, colTien, colPrint);

        ComboBox<EmployeeFilterOption> cbEmployeeFilter = new ComboBox<>();
        cbEmployeeFilter.setPrefWidth(520);
        cbEmployeeFilter.setPromptText("Lọc theo nhân viên lập hóa đơn");

        Button btnRefresh = new Button("Làm mới Hóa đơn");

        Runnable applyFilter = () -> {
            EmployeeFilterOption selected = cbEmployeeFilter.getValue();
            filteredInvoices.setPredicate(hd -> {
                if (hd == null) return false;
                
                // Role-based visibility
                if (loggedInAccount != null && loggedInAccount.getVaiTro() != VaiTro.ADMIN) {
                    String myBranch = loggedInAccount.getNhanVien() != null ? loggedInAccount.getNhanVien().getChiNhanh() : null;
                    if (myBranch == null || hd.getChiNhanh() == null || !myBranch.trim().equalsIgnoreCase(hd.getChiNhanh().trim())) {
                        return false;
                    }
                }

                // Additional employee filter (for Admin or Manager within branch)
                if (selected == null || selected.isAll()) return true;
                return hd.getNhanVien() != null && selected.getMaNhanVien().equals(hd.getNhanVien().getMaNhanVien());
            });
        };

        cbEmployeeFilter.setOnAction(e -> applyFilter.run());

        btnRefresh.setOnAction(e -> {
            CompletableFuture.supplyAsync(() -> {
                try { 
                    return RMICLientFactory.getHoaDonService().getAll();
                }
                catch (Exception ex) { 
                    ex.printStackTrace();
                    return null; 
                }
            }).thenAccept(data -> {
                if (data != null) {
                    Platform.runLater(() -> {
                        allInvoices.setAll(data);
                        applyFilter.run();
                    });
                }
            });
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                return RMICLientFactory.getEmployeeService().getAll();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).thenAccept(emps -> {
            if (emps == null) return;
            Platform.runLater(() -> {
                List<EmployeeFilterOption> items = new ArrayList<>();
                items.add(EmployeeFilterOption.allOption());
                for (NhanVien nv : emps) {
                    if (nv != null && nv.getMaNhanVien() != null) {
                        String name = nv.getHoTen() != null ? nv.getHoTen() : nv.getMaNhanVien();
                        String nick = (nv.getTaiKhoan() != null && nv.getTaiKhoan().getMaTaiKhoan() != null)
                                ? nv.getTaiKhoan().getMaTaiKhoan()
                                : "chưa có";
                        String label = String.format("%s | nick: %s | mã NV: %s", name, nick, nv.getMaNhanVien());
                        items.add(new EmployeeFilterOption(nv.getMaNhanVien(), label));
                    }
                }
                cbEmployeeFilter.getItems().setAll(items);
                cbEmployeeFilter.getSelectionModel().selectFirst();

                if (loggedInAccount != null && loggedInAccount.getVaiTro() == VaiTro.NHANVIEN && loggedInAccount.getNhanVien() != null) {
                    String myId = loggedInAccount.getNhanVien().getMaNhanVien();
                    cbEmployeeFilter.getItems().stream()
                            .filter(i -> myId != null && myId.equals(i.getMaNhanVien()))
                            .findFirst()
                            .ifPresent(i -> cbEmployeeFilter.getSelectionModel().select(i));
                    cbEmployeeFilter.setDisable(true);
                }
                applyFilter.run();
            });
        });
        
        VBox centerBox = new VBox(table);
        centerBox.setPadding(new Insets(10));
        VBox.setVgrow(table, Priority.ALWAYS);
        pane.setCenter(centerBox);

        HBox bottomBox = new HBox(12, new Label("Nhân viên:"), cbEmployeeFilter, btnRefresh);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(15));
        pane.setBottom(bottomBox);

        btnRefresh.fire();
        return pane;
    }

    private static final class EmployeeFilterOption {
        private final String maNhanVien;
        private final String label;

        private EmployeeFilterOption(String maNhanVien, String label) {
            this.maNhanVien = maNhanVien;
            this.label = label;
        }

        static EmployeeFilterOption allOption() {
            return new EmployeeFilterOption(null, "Tất cả nhân viên");
        }

        boolean isAll() {
            return maNhanVien == null;
        }

        String getMaNhanVien() {
            return maNhanVien;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void showInvoiceDetailsDialog(HoaDon hd) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi Tiết Hóa Đơn: " + hd.getMaHoaDon());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<ChiTietHoaDon> table = new TableView<>();
        
        TableColumn<ChiTietHoaDon, String> colSP = new TableColumn<>("Sản Phẩm");
        colSP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSanPham().getTenSP()));
        colSP.setPrefWidth(250);

        TableColumn<ChiTietHoaDon, Integer> colSL = new TableColumn<>("Số Lượng");
        colSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colSL.setPrefWidth(100);

        TableColumn<ChiTietHoaDon, Double> colGia = new TableColumn<>("Đơn Giá");
        colGia.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSanPham().getGiaBan()).asObject());
        colGia.setPrefWidth(120);
        formatCurrencyColumn(colGia);

        TableColumn<ChiTietHoaDon, String> colVAT = new TableColumn<>("Thuế VAT");
        colVAT.setCellValueFactory(c -> new SimpleStringProperty((c.getValue().getVAT() * 100) + "%"));
        colVAT.setPrefWidth(100);

        TableColumn<ChiTietHoaDon, Double> colThanhTien = new TableColumn<>("Thành Tiền");
        colThanhTien.setCellValueFactory(c -> {
            double total = c.getValue().getSoLuong() * c.getValue().getSanPham().getGiaBan() * (1 + c.getValue().getVAT());
            return new SimpleDoubleProperty(total).asObject();
        });
        colThanhTien.setPrefWidth(150);
        formatCurrencyColumn(colThanhTien);

        table.getColumns().addAll(colSP, colSL, colGia, colVAT, colThanhTien);

        CompletableFuture.supplyAsync(() -> {
            try { return RMICLientFactory.getHoaDonService().findByIdWithDetails(hd.getMaHoaDon()); }
            catch (Exception ex) { ex.printStackTrace(); return null; }
        }).thenAccept(fullHD -> {
            if (fullHD != null && fullHD.getChiTietHoaDons() != null) {
                Platform.runLater(() -> table.getItems().setAll(fullHD.getChiTietHoaDons()));
            }
        });

        Label lblSummary = new Label();
        lblSummary.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSummary.setText(String.format("Tổng cộng sản phẩm: %,.0f VNĐ | Điểm sử dụng: %d | Thực thanh toán: %,.0f VNĐ", 
            hd.getTienKhachHangPhaiThanhToan() + hd.getSoDiemTichLuyDuocSuDung(), 
            hd.getSoDiemTichLuyDuocSuDung(), 
            hd.getTienKhachHangPhaiThanhToan()));

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().setAll(new Label("Danh sách sản phẩm đã mua:"), table, lblSummary);
        dialog.getDialogPane().setContent(vbox);
        dialog.setWidth(800);
        dialog.setHeight(500);
        dialog.showAndWait();
    }

    private void previewInvoicePdf(HoaDon hd) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return RMICLientFactory.getHoaDonService().findByIdWithDetails(hd.getMaHoaDon());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).thenAccept(fullHD -> Platform.runLater(() -> {
            if (fullHD == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi hóa đơn", "Không tải được chi tiết hóa đơn.");
                return;
            }
            try {
                File pdfFile = File.createTempFile("hoa-don-" + fullHD.getMaHoaDon() + "-", ".pdf");
                buildInvoicePdf(fullHD, pdfFile);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "PDF đã tạo", "Đã tạo file PDF tại:\n" + pdfFile.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi preview PDF", "Không tạo được PDF hóa đơn: " + e.getMessage());
            }
        }));
    }

    private void buildInvoicePdf(HoaDon hd, File outputFile) throws Exception {
        Class<?> pdDocumentClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Class<?> pdPageClass = Class.forName("org.apache.pdfbox.pdmodel.PDPage");
        Class<?> pdRectangleClass = Class.forName("org.apache.pdfbox.pdmodel.common.PDRectangle");
        Class<?> pdFontClass = Class.forName("org.apache.pdfbox.pdmodel.font.PDType1Font");
        Class<?> pdType0FontClass = Class.forName("org.apache.pdfbox.pdmodel.font.PDType0Font");
        Class<?> pdfontClass = Class.forName("org.apache.pdfbox.pdmodel.font.PDFont");
        Class<?> contentStreamClass = Class.forName("org.apache.pdfbox.pdmodel.PDPageContentStream");

        Object document = pdDocumentClass.getConstructor().newInstance();
        try {
            Object a4 = pdRectangleClass.getField("A4").get(null);
            Object page = pdPageClass.getConstructor(pdRectangleClass).newInstance(a4);
            pdDocumentClass.getMethod("addPage", pdPageClass).invoke(document, page);

            Object fontBold = null;
            Object fontNormal = null;
            String os = System.getProperty("os.name", "").toLowerCase();
            String[] vnFontCandidates;
            if (os.contains("win")) {
                vnFontCandidates = new String[]{
                        "C:\\\\Windows\\\\Fonts\\\\arial.ttf",
                        "C:\\\\Windows\\\\Fonts\\\\arialuni.ttf",
                        "C:\\\\Windows\\\\Fonts\\\\times.ttf",
                        "C:\\\\Windows\\\\Fonts\\\\tahoma.ttf"
                };
            } else if (os.contains("mac")) {
                vnFontCandidates = new String[]{
                        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
                        "/System/Library/Fonts/Supplemental/Arial Unicode MS.ttf",
                        "/System/Library/Fonts/Supplemental/Arial.ttf",
                        "/System/Library/Fonts/Supplemental/Times New Roman.ttf"
                };
            } else {
                vnFontCandidates = new String[]{
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                        "/usr/share/fonts/truetype/freefont/FreeSans.ttf"
                };
            }

            for (String p : vnFontCandidates) {
                File f = new File(p);
                if (!f.exists()) continue;
                try {
                    Object loaded = pdType0FontClass
                            .getMethod("load", pdDocumentClass, File.class)
                            .invoke(null, document, f);
                    fontBold = loaded;
                    fontNormal = loaded;
                    break;
                } catch (Exception ignored) {
                    // thử font tiếp theo
                }
            }
            if (fontNormal == null) {
                // fallback cuối cùng: sẽ không hỗ trợ đầy đủ tiếng Việt.
                fontBold = pdFontClass.getField("HELVETICA_BOLD").get(null);
                fontNormal = pdFontClass.getField("HELVETICA").get(null);
            }

            Object content = contentStreamClass
                    .getConstructor(pdDocumentClass, pdPageClass)
                    .newInstance(document, page);
            try {
                float margin = 40f;
                float leading = 16f;

                contentStreamClass.getMethod("beginText").invoke(content);
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontBold, 16f);
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, margin, 800f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "PHARMACY MANAGEMENT SYSTEM");
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontBold, 14f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "HÓA ĐƠN BÁN LẺ");

                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontNormal, 11f);
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading * 2);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Mã hóa đơn  : " + safeText(hd.getMaHoaDon()));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Ngày lập    : " + safeText(String.valueOf(hd.getNgayLapHD())));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Thời gian lập: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Nhân viên   : " + safeText(hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : ""));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                String branch = hd.getNhanVien() != null && hd.getNhanVien().getChiNhanh() != null
                        ? hd.getNhanVien().getChiNhanh() : "Chưa gán";
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Chi nhánh   : " + safeText(branch));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Khách hàng  : " + safeText(hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "Khách lẻ"));

                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading * 2);
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontBold, 11f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "--------------------------------------------------------------------------");
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -14f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, String.format("%-26s %-4s %-12s %-14s", "Sản phẩm", "SL", "Đơn giá", "Thành tiền"));
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontNormal, 10f);

                double subtotal = 0;
                if (hd.getChiTietHoaDons() != null) {
                    for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                        double line = ct.getSoLuong() * ct.getSanPham().getGiaBan() * (1 + ct.getVAT());
                        subtotal += line;
                        contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -14f);
                        String ten = safeText(ct.getSanPham().getTenSP());
                        if (ten.length() > 24) ten = ten.substring(0, 24);
                        contentStreamClass.getMethod("showText", String.class).invoke(content, String.format("%-26s %-4d %-12s %-14s",
                                ten, ct.getSoLuong(), String.format("%,.0f", ct.getSanPham().getGiaBan()), String.format("%,.0f", line)));
                    }
                }

                double vat = subtotal * 0.08;
                double discount = hd.getSoDiemTichLuyDuocSuDung();
                double finalTotal = hd.getTienKhachHangPhaiThanhToan();

                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -22f);
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontBold, 11f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "--------------------------------------------------------------------------");
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -14f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Tạm tính       : " + String.format("%,.0f VND", subtotal));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Thuế VAT (8%)  : " + String.format("%,.0f VND", vat));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Giảm điểm      : " + String.format("%,.0f VND", discount));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Tổng thanh toán: " + String.format("%,.0f VND", finalTotal));
                contentStreamClass.getMethod("newLineAtOffset", float.class, float.class).invoke(content, 0f, -leading * 2);
                contentStreamClass.getMethod("setFont", pdfontClass, float.class).invoke(content, fontNormal, 10f);
                contentStreamClass.getMethod("showText", String.class).invoke(content, "Cảm ơn quý khách. Hẹn gặp lại!");
                contentStreamClass.getMethod("endText").invoke(content);
            } finally {
                contentStreamClass.getMethod("close").invoke(content);
            }

            pdDocumentClass.getMethod("save", File.class).invoke(document, outputFile);
        } finally {
            pdDocumentClass.getMethod("close").invoke(document);
        }
    }

    private String safeText(String text) {
        if (text == null) return "";
        return text
                .replace('\u0110', 'D')
                .replace('\u0111', 'd');
    }

    private void printInvoice(HoaDon hd) {
        // Keep legacy print flow if needed by other callers.
        CompletableFuture.supplyAsync(() -> {
            try {
                return RMICLientFactory.getHoaDonService().findByIdWithDetails(hd.getMaHoaDon());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).thenAccept(fullHD -> Platform.runLater(() -> {
            if (fullHD == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi in hóa đơn", "Không tải được chi tiết hóa đơn để in.");
                return;
            }

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi máy in", "Không tìm thấy máy in khả dụng.");
                return;
            }

            boolean accepted = job.showPrintDialog(null);
            if (!accepted) {
                job.cancelJob();
                return;
            }

            VBox printable = buildInvoicePrintNode(fullHD);
            boolean success = job.printPage(printable);
            if (success) {
                job.endJob();
                showAlert(Alert.AlertType.INFORMATION, "In hóa đơn", "Đã gửi lệnh in hóa đơn thành công.");
            } else {
                job.cancelJob();
                showAlert(Alert.AlertType.ERROR, "In hóa đơn", "In thất bại. Vui lòng thử lại.");
            }
        }));
    }

    private VBox buildInvoicePrintNode(HoaDon hd) {
        VBox root = new VBox(6);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-font-size: 12px;");
        root.setPrefWidth(500);

        Label title = new Label("PHARMACY MANAGEMENT SYSTEM");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        Label billNo = new Label("Mã hóa đơn: " + hd.getMaHoaDon());
        Label date = new Label("Ngày lập: " + hd.getNgayLapHD());
        Label staff = new Label("Nhân viên: " + (hd.getNhanVien() != null ? hd.getNhanVien().getHoTen() : ""));
        Label customer = new Label("Khách hàng: " + (hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "Khách lẻ"));

        root.getChildren().addAll(title, billNo, date, staff, customer, new Separator());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);
        grid.add(new Label("Sản phẩm"), 0, 0);
        grid.add(new Label("SL"), 1, 0);
        grid.add(new Label("Đơn giá"), 2, 0);
        grid.add(new Label("Thành tiền"), 3, 0);

        double subtotal = 0;
        int row = 1;
        if (hd.getChiTietHoaDons() != null) {
            for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                double line = ct.getSoLuong() * ct.getSanPham().getGiaBan() * (1 + ct.getVAT());
                subtotal += line;
                grid.add(new Label(ct.getSanPham().getTenSP()), 0, row);
                grid.add(new Label(String.valueOf(ct.getSoLuong())), 1, row);
                grid.add(new Label(String.format("%,.0f", ct.getSanPham().getGiaBan())), 2, row);
                grid.add(new Label(String.format("%,.0f", line)), 3, row);
                row++;
            }
        }

        double vat = subtotal * 0.08;
        double discount = hd.getSoDiemTichLuyDuocSuDung();
        double finalTotal = hd.getTienKhachHangPhaiThanhToan();

        root.getChildren().addAll(
                grid,
                new Separator(),
                new Label(String.format("Tạm tính: %,.0f VNĐ", subtotal)),
                new Label(String.format("Thuế VAT (8%%): %,.0f VNĐ", vat)),
                new Label(String.format("Giảm giá điểm: %,.0f VNĐ", discount)),
                new Label(String.format("Tổng thanh toán: %,.0f VNĐ", finalTotal))
        );
        return root;
    }

    private BorderPane createEmployeeManagementPane() {
        BorderPane pane = new BorderPane();
        
        // Header Box with Search and Add
        HBox topBox = new HBox(15);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10));

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo tên, email, sđt...");
        txtSearch.setPrefWidth(300);

        topBox.getChildren().addAll(new Label("Tìm kiếm:"), txtSearch);
        pane.setTop(topBox);

        employeeTable = new TableView<>();
        employeeList = FXCollections.observableArrayList();
        filteredEmployeeList = new FilteredList<>(employeeList, p -> true);
        employeeTable.setItems(filteredEmployeeList);

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredEmployeeList.setPredicate(nv -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return (nv.getHoTen() != null && nv.getHoTen().toLowerCase().contains(lowerCaseFilter)) ||
                       (nv.getEmail() != null && nv.getEmail().toLowerCase().contains(lowerCaseFilter)) ||
                       (nv.getSdt() != null && nv.getSdt().toLowerCase().contains(lowerCaseFilter));
            });
        });

        TableColumn<NhanVien, String> colHoTen = new TableColumn<>("Họ Tên"); colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen")); colHoTen.setPrefWidth(200);
        TableColumn<NhanVien, String> colSdt = new TableColumn<>("Số Điện Thoại"); colSdt.setCellValueFactory(new PropertyValueFactory<>("sdt")); colSdt.setPrefWidth(120);
        TableColumn<NhanVien, String> colEmail = new TableColumn<>("Email"); colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); colEmail.setPrefWidth(200);
        TableColumn<NhanVien, String> colCCCD = new TableColumn<>("CCCD"); colCCCD.setCellValueFactory(new PropertyValueFactory<>("cccd")); colCCCD.setPrefWidth(120);
        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Tình Trạng"); colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTinhTrangNhanVien() != null ? c.getValue().getTinhTrangNhanVien().name() : "")); colTrangThai.setPrefWidth(120);
        TableColumn<NhanVien, String> colTaiKhoan = new TableColumn<>("Tài Khoản"); colTaiKhoan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaiKhoan() != null ? c.getValue().getTaiKhoan().getMaTaiKhoan() : "")); colTaiKhoan.setPrefWidth(120);
        TableColumn<NhanVien, String> colVaiTro = new TableColumn<>("Vai Trò"); colVaiTro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaiKhoan() != null ? c.getValue().getTaiKhoan().getVaiTro().name() : "")); colVaiTro.setPrefWidth(100);
        TableColumn<NhanVien, String> colChiNhanh = new TableColumn<>("Chi Nhánh");
        colChiNhanh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChiNhanh() != null ? c.getValue().getChiNhanh() : ""));
        colChiNhanh.setPrefWidth(160);
        TableColumn<NhanVien, String> colQuanLy = new TableColumn<>("Quản Lý");
        colQuanLy.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getQuanLy() != null ? c.getValue().getQuanLy().getHoTen() : ""));
        colQuanLy.setPrefWidth(170);
        
        TableColumn<NhanVien, String> colRevenue = new TableColumn<>("Doanh Thu");
        colRevenue.setCellValueFactory(c -> {
            Double rev = employeeRevenueMap.get(c.getValue().getHoTen());
            return new SimpleStringProperty(rev != null ? String.format("%,.0f VNĐ", rev) : "0 VNĐ");
        });
        colRevenue.setPrefWidth(150);

        employeeTable.getColumns().addAll(colHoTen, colSdt, colEmail, colCCCD, colTrangThai, colTaiKhoan, colVaiTro, colChiNhanh, colQuanLy, colRevenue);

        VBox centerBox = new VBox(new Label("QUẢN LÝ NHÂN VIÊN VÀ QUYỀN HỆ THỐNG"), employeeTable);
        centerBox.setSpacing(10);
        centerBox.setPadding(new Insets(10));
        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        pane.setCenter(centerBox);

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setOnAction(e -> refreshEmployeeList());

        Button btnAdd = new Button("Thêm");
        btnAdd.setOnAction(e -> {
            EmployeeDialog dialog = new EmployeeDialog(this, null);
            dialog.showAndWait();
        });

        Button btnEdit = new Button("Sửa");
        btnEdit.setOnAction(e -> editEmployee());

        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteEmployee());

        Button btnResetPassword = new Button("Reset mật khẩu");
        btnResetPassword.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        btnResetPassword.setOnAction(e -> resetEmployeePassword());
        boolean isAdmin = loggedInAccount != null && loggedInAccount.getVaiTro() == VaiTro.ADMIN;
        btnResetPassword.setDisable(!isAdmin);

        bottomBox.getChildren().addAll(btnRefresh, btnAdd, btnEdit, btnDelete, btnResetPassword);
        pane.setBottom(bottomBox);

        refreshEmployeeList();
        return pane;
    }

    public void refreshEmployeeList() {
        CompletableFuture.runAsync(() -> {
            try {
                // Fetch Revenue stats first
                List<Object[]> stats = RMICLientFactory.getThongKeService().getHieuSuatNhanVien(LocalDate.now().minusYears(10), LocalDate.now());
                java.util.Map<String, Double> tempMap = new java.util.HashMap<>();
                for (Object[] row : stats) {
                    tempMap.put(row[0].toString(), Double.parseDouble(row[2].toString()));
                }
                
                // Fetch Employees
                List<NhanVien> emps = RMICLientFactory.getEmployeeService().getAll();
                List<NhanVien> filtered = new java.util.ArrayList<>();
                boolean isAdmin = loggedInAccount != null && loggedInAccount.getVaiTro() == VaiTro.ADMIN;
                boolean isManager = loggedInAccount != null && loggedInAccount.getVaiTro() == VaiTro.QUANLY;
                NhanVien managerUser = loggedInAccount != null ? loggedInAccount.getNhanVien() : null;
                String managerBranch = managerUser != null ? managerUser.getChiNhanh() : null;

                if (emps != null) {
                    if (loggedInAccount == null) {
                        // Clear list or show nothing
                    } else if (isAdmin) {
                        filtered.addAll(emps);
                    } else if (isManager && managerUser != null) {
                        for (NhanVien nv : emps) {
                            boolean sameBranch = managerBranch != null && managerBranch.equalsIgnoreCase(nv.getChiNhanh());
                            // Manager sees everyone in their branch
                            if (sameBranch) {
                                filtered.add(nv);
                            }
                        }
                    }
                }
                
                Platform.runLater(() -> {
                    employeeRevenueMap.clear();
                    employeeRevenueMap.putAll(tempMap);
                    employeeList.setAll(filtered);
                    employeeTable.refresh();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void editEmployee() {
        NhanVien selectedItem = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        EmployeeDialog dialog = new EmployeeDialog(this, selectedItem);
        dialog.showAndWait();
    }

    private void deleteEmployee() {
        NhanVien selectedItem = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        
        if (selectedItem.getTaiKhoan() != null && "admin".equalsIgnoreCase(selectedItem.getTaiKhoan().getMaTaiKhoan())) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản Admin hệ thống gốc!");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Chắc chắn xóa nhân viên này?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    // Because TaiKhoan holds foreign key, we need to handle it or let cascade remove it.
                    // But in entities, there's no cascade from NhanVien to TaiKhoan, TaiKhoan might be orphaned.
                    // But it's okay for now, we just delete NhanVien. Actually TaiKhoan is master of relationship if NhanVien has @JoinColumn. Wait, NhanVien has @JoinColumn.
                    // NhanVien is the owner. If we delete NhanVien, we might leave TaiKhoan behind. Let's delete TaiKhoan too.
                    TaiKhoan tk = selectedItem.getTaiKhoan();
                    boolean deleted = RMICLientFactory.getEmployeeService().delete(selectedItem.getMaNhanVien());
                    if (deleted && tk != null) {
                        RMICLientFactory.getAccountService().delete(tk.getMaTaiKhoan());
                    }
                    refreshEmployeeList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi xóa", "Không thể xóa nhân viên: " + ex.getMessage());
                }
            }
        });
    }

    private void resetEmployeePassword() {
        if (loggedInAccount == null || loggedInAccount.getVaiTro() != VaiTro.ADMIN) {
            showAlert(Alert.AlertType.ERROR, "Không có quyền", "Chỉ ADMIN mới có quyền reset mật khẩu.");
            return;
        }

        NhanVien selectedItem = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn nhân viên", "Vui lòng chọn nhân viên cần reset mật khẩu.");
            return;
        }
        if (selectedItem.getTaiKhoan() == null || selectedItem.getTaiKhoan().getMaTaiKhoan() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu tài khoản", "Nhân viên này chưa có tài khoản đăng nhập.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Reset mật khẩu cho " + selectedItem.getHoTen() + " về '12345678'?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    boolean success = RMICLientFactory.getAccountService().resetPassword(selectedItem.getTaiKhoan().getMaTaiKhoan());
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được reset về '12345678'");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể reset mật khẩu.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Lỗi: " + ex.getMessage());
                }
            }
        });
    }


    private BorderPane createProductManagementPane() {
        BorderPane pane = new BorderPane();
        
        productTable = new TableView<>();
        productList = FXCollections.observableArrayList();
        filteredProductList = new FilteredList<>(productList, p -> true);
        productTable.setItems(filteredProductList);

        TableColumn<SanPham, String> colMaSP = new TableColumn<>("Mã SP");
        colMaSP.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        colMaSP.setPrefWidth(150);

        TableColumn<SanPham, String> colTenSP = new TableColumn<>("Tên Sản Phẩm");
        colTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colTenSP.setPrefWidth(250);

        TableColumn<SanPham, String> colThuongHieu = new TableColumn<>("Thương Hiệu");
        colThuongHieu.setCellValueFactory(new PropertyValueFactory<>("thuongHieu"));
        colThuongHieu.setPrefWidth(150);

        TableColumn<SanPham, LocalDate> colHanSD = new TableColumn<>("Hạn Sử Dụng");
        colHanSD.setCellValueFactory(new PropertyValueFactory<>("hanSD"));
        colHanSD.setPrefWidth(130);

        TableColumn<SanPham, String> colLoaiSP = new TableColumn<>("Loại SP");
        colLoaiSP.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLoaiSP().getLoaiSP()));
        colLoaiSP.setPrefWidth(150);

        TableColumn<SanPham, String> colChiNhanhView = new TableColumn<>("Chi nhánh xem");
        colChiNhanhView.setCellValueFactory(cellData -> new SimpleStringProperty(
                (currentProductViewBranch == null || currentProductViewBranch.isBlank()) ? "Kho tổng" : currentProductViewBranch
        ));
        colChiNhanhView.setPrefWidth(150);

        TableColumn<SanPham, Double> colGiaBan = new TableColumn<>("Giá Bán");
        colGiaBan.setCellValueFactory(new PropertyValueFactory<>("giaBan"));
        colGiaBan.setPrefWidth(120);
        formatCurrencyColumn(colGiaBan);

        TableColumn<SanPham, Integer> colTon = new TableColumn<>("Số Lượng Tồn");
        colTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colTon.setPrefWidth(120);
        colTon.setCellFactory(column -> {
            return new TableCell<SanPham, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        SanPham sp = getTableView().getItems().get(getIndex());
                        String color = isLowStock(sp) ? "#c0392b" : "#2980b9";
                        setStyle("-fx-text-fill: " + color + "; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");
                        setOnMouseClicked(event -> {
                            new StockHistoryDialog(sp, currentProductViewBranch).show();
                        });
                    }
                }
            };
        });

        TableColumn<SanPham, Integer> colNguongTon = new TableColumn<>("Ngưỡng Tồn");
        colNguongTon.setCellValueFactory(new PropertyValueFactory<>("nguongTonToiThieu"));
        colNguongTon.setPrefWidth(110);

        TableColumn<SanPham, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setCellValueFactory(cellData -> new SimpleStringProperty(buildProductAlertNote(cellData.getValue())));
        colGhiChu.setPrefWidth(220);
        colGhiChu.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("hết hạn")) {
                        setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
                    } else if (item.contains("tồn thấp")) {
                        setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        TableColumn<SanPham, Void> colTrangThaiBan = new TableColumn<>("Bật/Tắt");
        colTrangThaiBan.setPrefWidth(120);
        colTrangThaiBan.setCellFactory(tc -> new TableCell<>() {
            private final Button btnToggle = new Button();
            {
                btnToggle.setOnAction(e -> {
                    SanPham sp = getTableView().getItems().get(getIndex());
                    if (sp != null) {
                        toggleProductSaleStatus(sp);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                SanPham sp = getTableView().getItems().get(getIndex());
                boolean enabled = isProductEnabledForSale(sp);
                btnToggle.setText(enabled ? "ON" : "OFF");
                btnToggle.setStyle(enabled
                        ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;"
                        : "-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
                setGraphic(btnToggle);
            }
        });

        TableColumn<SanPham, String> colQR = new TableColumn<>("Mã QR");
        colQR.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaSP()));
        colQR.setPrefWidth(140);
        colQR.setCellFactory(column -> new TableCell<SanPham, String>() {
            private final Button btnView = new Button("Xem QR");
            {
                btnView.setStyle("-fx-background-color: #16a085; -fx-text-fill: white;");
                btnView.setOnAction(e -> {
                    SanPham sp = getTableView().getItems().get(getIndex());
                    showProductQrDialog(sp);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnView);
                }
            }
        });

        productTable.getColumns().addAll(colMaSP, colTenSP, colThuongHieu, colHanSD, colLoaiSP, colChiNhanhView, colGiaBan, colTon, colNguongTon, colGhiChu, colTrangThaiBan, colQR);

        productTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null) {
                    setStyle("");
                    return;
                }
                if (isExpiryWithinMonths(sp, 3) || isLowStock(sp)) {
                    setStyle("-fx-background-color: #fdeaea;");
                } else if (isExpiryWithinMonths(sp, 6)) {
                    setStyle("-fx-background-color: #fff6e5;");
                } else {
                    setStyle("");
                }
            }
        });

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã SP / tên SP / thương hiệu...");
        txtSearch.setPrefWidth(320);

        ComboBox<String> cbLoai = new ComboBox<>();
        cbLoai.getItems().addAll("Tất cả loại", "thuốc", "thực phẩm chức năng");
        cbLoai.setValue("Tất cả loại");
        cbLoai.setPrefWidth(170);

        ComboBox<String> cbTrangThaiBan = new ComboBox<>();
        cbTrangThaiBan.getItems().addAll("Tất cả trạng thái", "ON (đang bán)", "OFF (tắt bán)");
        cbTrangThaiBan.setValue("Tất cả trạng thái");
        cbTrangThaiBan.setPrefWidth(170);

        ComboBox<String> cbCanhBao = new ComboBox<>();
        cbCanhBao.getItems().addAll(
                "Tất cả cảnh báo",
                "Sắp hết hạn < 3 tháng",
                "Sắp hết hạn < 6 tháng",
                "Đã hết hạn",
                "Tồn kho thấp"
        );
        cbCanhBao.setValue("Tất cả cảnh báo");
        cbCanhBao.setPrefWidth(190);

        cbProductBranch = new ComboBox<>();
        cbProductBranch.setPrefWidth(170);
        cbProductBranch.setPromptText("Chi nhánh");
        cbProductBranch.setOnAction(e -> {
            String selected = cbProductBranch.getValue();
            if ("Kho tổng".equals(selected)) {
                currentProductViewBranch = null;
            } else {
                currentProductViewBranch = selected;
            }
            loadDataAsync();
            productTable.refresh();
        });
        initProductBranchFilter();

        Runnable applyProductFilter = () -> {
            String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
            String loai = cbLoai.getValue();
            String trangThai = cbTrangThaiBan.getValue();
            String canhBao = cbCanhBao.getValue();

            filteredProductList.setPredicate(sp -> {
                if (sp == null) return false;

                boolean matchKeyword = keyword.isEmpty()
                        || (sp.getMaSP() != null && sp.getMaSP().toLowerCase().contains(keyword))
                        || (sp.getTenSP() != null && sp.getTenSP().toLowerCase().contains(keyword))
                        || (sp.getThuongHieu() != null && sp.getThuongHieu().toLowerCase().contains(keyword));
                if (!matchKeyword) return false;

                if (!"Tất cả loại".equals(loai)) {
                    String loaiSp = (sp.getLoaiSP() == null || sp.getLoaiSP().getLoaiSP() == null)
                            ? ""
                            : sp.getLoaiSP().getLoaiSP().toLowerCase();
                    if (!loaiSp.equals(loai)) return false;
                }

                if ("ON (đang bán)".equals(trangThai) && !isProductEnabledForSale(sp)) return false;
                if ("OFF (tắt bán)".equals(trangThai) && isProductEnabledForSale(sp)) return false;

                if ("Sắp hết hạn < 3 tháng".equals(canhBao) && !isExpiryWithinMonths(sp, 3)) return false;
                if ("Sắp hết hạn < 6 tháng".equals(canhBao) && !isExpiryWithinMonths(sp, 6)) return false;
                if ("Đã hết hạn".equals(canhBao)) {
                    if (sp.getHanSD() == null || !sp.getHanSD().isBefore(LocalDate.now())) return false;
                }
                if ("Tồn kho thấp".equals(canhBao) && !isLowStock(sp)) return false;

                return true;
            });
        };

        txtSearch.textProperty().addListener((obs, o, n) -> applyProductFilter.run());
        cbLoai.setOnAction(e -> applyProductFilter.run());
        cbTrangThaiBan.setOnAction(e -> applyProductFilter.run());
        cbCanhBao.setOnAction(e -> applyProductFilter.run());

        HBox filterBox = new HBox(10,
                new Label("Tìm kiếm:"),
                txtSearch,
                new Label("Chi nhánh:"),
                cbProductBranch,
                new Label("Loại:"),
                cbLoai,
                new Label("Bán:"),
                cbTrangThaiBan,
                new Label("Cảnh báo:"),
                cbCanhBao
        );
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(8, 10, 8, 10));

        VBox centerBox = new VBox(filterBox, productTable);
        centerBox.setPadding(new Insets(10));
        VBox.setVgrow(productTable, Priority.ALWAYS);
        pane.setCenter(centerBox);

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setOnAction(e -> loadDataAsync());

        Button btnAdd = new Button("Thêm");
        btnAdd.setOnAction(e -> {
            ProductDialog dialog = new ProductDialog(this, null);
            dialog.showAndWait();
        });

        Button btnEdit = new Button("Sửa");
        btnEdit.setOnAction(e -> editProduct());

        Button btnDelete = new Button("Xóa");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteProduct());

        bottomBox.getChildren().addAll(btnRefresh, btnAdd, btnEdit, btnDelete);
        pane.setBottom(bottomBox);

        return pane;
    }

    private SplitPane createPOSPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6);

        // LEFT: Product List
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        
        TextField txtSearchProduct = new TextField();
        txtSearchProduct.setPromptText("Tìm kiếm sản phẩm theo tên...");
        txtSearchProduct.textProperty().addListener((obs, oldV, newV) -> {
            filteredProductList.setPredicate(p -> {
                if (newV == null || newV.isEmpty()) return true;
                return p.getTenSP().toLowerCase().contains(newV.toLowerCase());
            });
        });

        posProductTable = new TableView<>();
        posProductTable.setItems(filteredProductList);
        
        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên SP");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colTen.setPrefWidth(300);
        
        TableColumn<SanPham, Double> colGia = new TableColumn<>("Giá Bán");
        colGia.setCellValueFactory(new PropertyValueFactory<>("giaBan"));
        colGia.setPrefWidth(120);
        formatCurrencyColumn(colGia);

        posProductTable.getColumns().addAll(colTen, colGia);
        VBox.setVgrow(posProductTable, Priority.ALWAYS);

        posProductTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SanPham sp = posProductTable.getSelectionModel().getSelectedItem();
                if (sp != null) addToCart(sp);
            }
        });

        HBox searchProductBox = new HBox(10);
        searchProductBox.setAlignment(Pos.CENTER_LEFT);
        TextField txtScanCode = new TextField();
        txtScanCode.setPromptText("Quét/Nhập mã SP rồi Enter");
        txtScanCode.setPrefWidth(260);
        txtScanCode.setOnAction(e -> {
            addProductByCode(txtScanCode.getText());
            txtScanCode.clear();
        });

        Button btnScan = new Button("Quét Barcode");
        btnScan.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnScan.setOnAction(e -> {
            new BarcodeScannerDialog(code -> {
                // Tìm sản phẩm có mã khớp với code đã quét
                SanPham found = productList.stream()
                        .filter(p -> p.getMaSP().equals(code))
                        .findFirst()
                        .orElse(null);
                
                if (found != null) {
                    if (isProductEnabledForSale(found)) {
                        addToCart(found);
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm sản phẩm: " + found.getTenSP());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Sản phẩm đang OFF", "Sản phẩm này đang tắt bán, không thể thêm vào hóa đơn.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Không tìm thấy", "Không tìm thấy sản phẩm có mã: " + code);
                }
            }).show();
        });
        searchProductBox.getChildren().addAll(txtSearchProduct, txtScanCode, btnScan);

        Label lblQrHint = new Label("Chọn 1 sản phẩm để tạo mã QR (dùng điện thoại chụp lại)");
        ImageView qrPreview = new ImageView();
        qrPreview.setFitWidth(170);
        qrPreview.setFitHeight(170);
        qrPreview.setPreserveRatio(true);
        Label lblQrCode = new Label("Mã: ---");

        VBox qrBox = new VBox(6, lblQrHint, qrPreview, lblQrCode);
        qrBox.setAlignment(Pos.CENTER_LEFT);
        qrBox.setPadding(new Insets(6, 0, 0, 0));
        qrBox.setStyle("-fx-border-color: #d0d0d0; -fx-border-radius: 6; -fx-padding: 8;");

        posProductTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selected) -> {
            if (selected == null || selected.getMaSP() == null) {
                qrPreview.setImage(null);
                lblQrCode.setText("Mã: ---");
                return;
            }
            qrPreview.setImage(generateQrImage(selected.getMaSP(), 170, 170));
            lblQrCode.setText("Mã: " + selected.getMaSP());
        });

        leftPane.getChildren().addAll(new Label("Danh Sách Sản Phẩm (Click đúp để thêm)"), searchProductBox, posProductTable, qrBox);

        // RIGHT: Cart & Invoice
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));

        cartTable = new TableView<>();
        cartList = FXCollections.observableArrayList();
        cartTable.setItems(cartList);
        cartTable.setEditable(true);

        TableColumn<CartItem, String> colCartTen = new TableColumn<>("Tên SP");
        colCartTen.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colCartTen.setPrefWidth(150);

        TableColumn<CartItem, Integer> colCartSL = new TableColumn<>("SL");
        colCartSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colCartSL.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colCartSL.setOnEditCommit(event -> {
            event.getRowValue().setSoLuong(event.getNewValue());
            calculateTotal();
        });
        colCartSL.setPrefWidth(50);

        TableColumn<CartItem, Double> colCartTotal = new TableColumn<>("Thành Tiền");
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colCartTotal.setPrefWidth(120);
        formatCurrencyColumn(colCartTotal);

        cartTable.getColumns().addAll(colCartTen, colCartSL, colCartTotal);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        
        Button btnRemoveItem = new Button("Xóa SP khỏi giỏ");
        btnRemoveItem.setOnAction(e -> {
            CartItem selected = cartTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cartList.remove(selected);
                calculateTotal();
            }
        });

        // Customer Section
        VBox customerBox = new VBox(5);
        customerBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 10;");
        
        HBox searchCusBox = new HBox(5);
        txtSearchPhone = new TextField();
        txtSearchPhone.setPromptText("SĐT Khách hàng");
        Button btnFindCus = new Button("Tìm");
        btnFindCus.setOnAction(e -> findCustomer());
        searchCusBox.getChildren().addAll(txtSearchPhone, btnFindCus);

        lblCusName = new Label("Khách: Chưa có");
        lblCusPoints = new Label("Điểm TL: 0");
        chkUsePoints = new CheckBox("Dùng điểm giảm giá");
        chkUsePoints.setDisable(true);
        chkUsePoints.setOnAction(e -> calculateTotal());

        customerBox.getChildren().addAll(new Label("Thông Tin Khách Hàng:"), searchCusBox, lblCusName, lblCusPoints, chkUsePoints);

        // Totals
        VBox totalBox = new VBox(5);
        lblTotal = new Label("Tạm tính: 0 VNĐ");
        lblVAT = new Label("Thuế VAT (8%): 0 VNĐ");
        lblDiscount = new Label("Giảm giá: 0 VNĐ");
        lblFinalTotal = new Label("Tổng cộng: 0 VNĐ");
        lblFinalTotal.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblFinalTotal.setStyle("-fx-text-fill: #e74c3c;");

        totalBox.getChildren().addAll(lblTotal, lblVAT, lblDiscount, lblFinalTotal);

        Button btnCheckout = new Button("THANH TOÁN LƯU HÓA ĐƠN");
        btnCheckout.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        btnCheckout.setOnAction(e -> checkout());

        rightPane.getChildren().addAll(new Label("Giỏ Hàng"), cartTable, btnRemoveItem, customerBox, totalBox, btnCheckout);

        splitPane.getItems().addAll(leftPane, rightPane);
        return splitPane;
    }

    private void addProductByCode(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            return;
        }
        SanPham found = productList.stream()
                .filter(p -> p.getMaSP() != null && p.getMaSP().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
        if (found == null) {
            showAlert(Alert.AlertType.WARNING, "Không tìm thấy", "Không có sản phẩm với mã: " + code);
            return;
        }
        if (!isProductEnabledForSale(found)) {
            showAlert(Alert.AlertType.WARNING, "Sản phẩm đang OFF", "Sản phẩm này đang tắt bán, không thể thêm vào hóa đơn.");
            return;
        }
        addToCart(found);
    }

    private WritableImage generateQrImage(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            WritableImage image = new WritableImage(width, height);
            PixelWriter writer = image.getPixelWriter();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return image;
        } catch (Exception e) {
            return null;
        }
    }

    private void showProductQrDialog(SanPham sp) {
        if (sp == null || sp.getMaSP() == null || sp.getMaSP().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu mã", "Sản phẩm chưa có mã để tạo QR.");
            return;
        }
        WritableImage qr = generateQrImage(sp.getMaSP(), 260, 260);
        if (qr == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi QR", "Không tạo được mã QR cho sản phẩm.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mã QR Sản Phẩm");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ImageView view = new ImageView(qr);
        view.setFitWidth(260);
        view.setFitHeight(260);
        view.setPreserveRatio(true);

        Label name = new Label("Sản phẩm: " + sp.getTenSP());
        Label code = new Label("Mã: " + sp.getMaSP());
        VBox box = new VBox(10, name, code, view);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void addToCart(SanPham sp) {
        if (!isProductEnabledForSale(sp)) {
            showAlert(Alert.AlertType.WARNING, "Sản phẩm đang OFF", "Sản phẩm này đang tắt bán, không thể thêm vào hóa đơn.");
            return;
        }
        for (CartItem item : cartList) {
            if (item.getSanPham().getMaSP().equals(sp.getMaSP())) {
                item.setSoLuong(item.getSoLuong() + 1);
                calculateTotal();
                return;
            }
        }
        cartList.add(new CartItem(sp, 1));
        calculateTotal();
    }

    private void calculateTotal() {
        double subtotal = 0;
        for (CartItem item : cartList) {
            subtotal += item.getThanhTien();
        }
        lblTotal.setText(String.format("Tạm tính: %,.0f VNĐ", subtotal));

        double vat = subtotal * 0.08;
        lblVAT.setText(String.format("Thuế VAT (8%%): %,.0f VNĐ", vat));

        double totalWithVAT = subtotal + vat;

        double discount = 0;
        if (currentCustomer != null && chkUsePoints.isSelected()) {
            discount = currentCustomer.getDiemTichLuy();
            if (discount > totalWithVAT) discount = totalWithVAT;
        }
        lblDiscount.setText(String.format("Giảm giá: %,.0f VNĐ", discount));
        
        double finalTotal = totalWithVAT - discount;
        lblFinalTotal.setText(String.format("Tổng cộng: %,.0f VNĐ", finalTotal));
    }

    private void findCustomer() {
        String phone = txtSearchPhone.getText().trim();
        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng nhập số điện thoại để tìm kiếm.");
            return;
        }

        // Validate VN phone format (Starts with 0, total 10 digits)
        if (!phone.matches("^0\\d{9}$")) {
            showAlert(Alert.AlertType.WARNING, "Sai định dạng", "Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 số, bắt đầu bằng số 0 (VD: 0899449596).");
            return;
        }
        
        try {
            KhachHangService khService = RMICLientFactory.getKhachHangService();
            List<KhachHang> khs = khService.getAll();
            currentCustomer = khs.stream().filter(k -> k.getSoDienThoai().equals(phone)).findFirst().orElse(null);
            
            if (currentCustomer != null) {
                lblCusName.setText("Khách: " + currentCustomer.getHoTen());
                lblCusPoints.setText(String.format("Điểm TL: %,.0f", currentCustomer.getDiemTichLuy()));
                chkUsePoints.setDisable(currentCustomer.getDiemTichLuy() <= 0);
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Không tìm thấy khách hàng với SĐT này! Bạn có muốn tạo mới không?", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText(null);
                alert.setTitle("Khách hàng mới");
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        KhachHang newKh = new KhachHang();
                        newKh.setSoDienThoai(phone);
                        CustomerDialog dialog = new CustomerDialog(this, newKh);
                        dialog.showAndWait();
                        
                        // Sau khi đóng dialog, thử load lại danh sách và tìm lại khách hàng này
                        try {
                            List<KhachHang> updatedKhs = RMICLientFactory.getKhachHangService().getAll();
                            currentCustomer = updatedKhs.stream().filter(k -> k.getSoDienThoai().equals(phone)).findFirst().orElse(null);
                            if (currentCustomer != null) {
                                lblCusName.setText("Khách: " + currentCustomer.getHoTen());
                                lblCusPoints.setText(String.format("Điểm TL: %,.0f", currentCustomer.getDiemTichLuy()));
                                chkUsePoints.setDisable(currentCustomer.getDiemTichLuy() <= 0);
                            } else {
                                // Nếu người dùng bấm Hủy trong Dialog thì currentCustomer vẫn null
                                lblCusName.setText("Khách: Chưa có");
                                lblCusPoints.setText("Điểm TL: 0");
                                chkUsePoints.setDisable(true);
                                chkUsePoints.setSelected(false);
                            }
                        } catch (Exception e) {}
                    } else {
                        lblCusName.setText("Khách: Chưa có");
                        lblCusPoints.setText("Điểm TL: 0");
                        chkUsePoints.setDisable(true);
                        chkUsePoints.setSelected(false);
                    }
                });
            }
            calculateTotal();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi tìm khách hàng!");
        }
    }

    private void checkout() {
        if (loggedInAccount == null || loggedInAccount.getNhanVien() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa đăng nhập", "Bạn cần đăng nhập để tạo hóa đơn bán hàng!");
            return;
        }
        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ!");
            return;
        }

        try {
            double subtotal = cartList.stream().mapToDouble(CartItem::getThanhTien).sum();
            double vatAmount = subtotal * 0.08;
            double totalWithVAT = subtotal + vatAmount;
            
            double discount = (currentCustomer != null && chkUsePoints.isSelected()) ? Math.min(currentCustomer.getDiemTichLuy(), totalWithVAT) : 0;
            double finalTotal = totalWithVAT - discount;

            HoaDon hd = new HoaDon();
            hd.setNgayLapHD(LocalDate.now());
            hd.setNhanVien(loggedInAccount.getNhanVien());
            hd.setKhachHang(currentCustomer); 
            hd.setChiNhanh(loggedInAccount.getNhanVien() != null ? loggedInAccount.getNhanVien().getChiNhanh() : null);
            hd.setSoDiemTichLuyDuocSuDung((int) discount);
            hd.setTienKhachHangPhaiThanhToan(finalTotal);

            List<ChiTietHoaDon> cthds = new ArrayList<>();
            for (CartItem item : cartList) {
                ChiTietHoaDon cthd = new ChiTietHoaDon();
                cthd.setHoaDon(hd);
                cthd.setSanPham(item.getSanPham());
                cthd.setSoLuong(item.getSoLuong());
                cthd.setVAT(0.08); 
                cthds.add(cthd);
            }
            hd.setChiTietHoaDons(cthds);

            HoaDonService hdService = RMICLientFactory.getHoaDonService();
            boolean success = hdService.save(hd);

            if (success) {
                if (currentCustomer != null) {
                    // Tích lũy 10% giá trị đơn hàng sau thanh toán
                    double newPoints = (finalTotal * 0.1) + (currentCustomer.getDiemTichLuy() - discount);
                    currentCustomer.setDiemTichLuy(newPoints);
                    RMICLientFactory.getKhachHangService().update(currentCustomer);
                }

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu hóa đơn thành công!");
                
                cartList.clear();
                currentCustomer = null;
                txtSearchPhone.clear();
                lblCusName.setText("Khách: Chưa có");
                lblCusPoints.setText("Điểm TL: 0");
                chkUsePoints.setSelected(false);
                chkUsePoints.setDisable(true);
                calculateTotal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Lưu hóa đơn thất bại!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Server", "Lỗi: " + ex.getMessage());
        }
    }

    public void loadData() {
        loadDataAsync();
    }

    private void loadDataAsync() {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        if (loggedInAccount != null && loggedInAccount.getVaiTro() == VaiTro.ADMIN) {
                            String branch = currentProductViewBranch;
                            if ("Kho tổng".equalsIgnoreCase(branch)) branch = null;
                            return RMICLientFactory.getSanPhamService().getAllByChiNhanh(branch);
                        }
                        if (loggedInAccount != null
                                && loggedInAccount.getNhanVien() != null
                                && loggedInAccount.getNhanVien().getChiNhanh() != null) {
                            currentProductViewBranch = loggedInAccount.getNhanVien().getChiNhanh();
                            return RMICLientFactory.getSanPhamService().getAllByChiNhanh(currentProductViewBranch);
                        }
                        return RMICLientFactory.getSanPhamService().getAll();
                    }
                    catch (Exception e) { throw new RuntimeException(e); }
                })
                .thenAccept(products -> Platform.runLater(() -> productList.setAll(products)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi tải DB: " + ex.getCause().getMessage()));
                    return null;
                });
    }

    private void initProductBranchFilter() {
        if (cbProductBranch == null) return;
        if (loggedInAccount == null || loggedInAccount.getNhanVien() == null) {
            cbProductBranch.getItems().setAll("Kho tổng");
            cbProductBranch.setValue("Kho tổng");
            cbProductBranch.setDisable(true);
            currentProductViewBranch = null;
            return;
        }

        if (loggedInAccount.getVaiTro() == VaiTro.ADMIN) {
            try {
                Set<String> branches = new HashSet<>();
                List<NhanVien> allNvs = RMICLientFactory.getEmployeeService().getAll();
                for (NhanVien nv : allNvs) {
                    if (nv != null && nv.getChiNhanh() != null && !nv.getChiNhanh().isBlank()) {
                        branches.add(nv.getChiNhanh());
                    }
                }
                List<String> items = new ArrayList<>();
                items.add("Kho tổng");
                items.addAll(branches.stream().sorted().toList());
                cbProductBranch.getItems().setAll(items);
                cbProductBranch.setDisable(false);
                if (cbProductBranch.getValue() == null) {
                    cbProductBranch.setValue("Kho tổng");
                }
                currentProductViewBranch = "Kho tổng".equals(cbProductBranch.getValue()) ? null : cbProductBranch.getValue();
            } catch (Exception e) {
                cbProductBranch.getItems().setAll("Kho tổng");
                cbProductBranch.setValue("Kho tổng");
                cbProductBranch.setDisable(false);
                currentProductViewBranch = null;
            }
        } else {
            String branch = loggedInAccount.getNhanVien().getChiNhanh();
            cbProductBranch.getItems().setAll(branch);
            cbProductBranch.setValue(branch);
            cbProductBranch.setDisable(true);
            currentProductViewBranch = branch;
        }
    }

    private void editProduct() {
        SanPham selectedItem = productTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        ProductDialog dialog = new ProductDialog(this, selectedItem);
        dialog.showAndWait();
    }

    private void deleteProduct() {
        SanPham selectedItem = productTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Chắc chắn xóa?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    RMICLientFactory.getSanPhamService().delete(selectedItem.getMaSP());
                    loadDataAsync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showLoginDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng nhập");

        ButtonType loginButtonType = new ButtonType("Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtUser = new TextField();
        PasswordField txtPass = new PasswordField();
        grid.add(new Label("Tài khoản:"), 0, 0); grid.add(txtUser, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1); grid.add(txtPass, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(txtUser::requestFocus);

        dialog.setResultConverter(b -> (b == loginButtonType) ? loginButtonType : null);

        dialog.showAndWait().ifPresent(b -> {
            if (b == loginButtonType) {
                try {
                    TaiKhoan account = RMICLientFactory.getAccountService().logIn(txtUser.getText().trim(), txtPass.getText());
                    if (account != null) {
                        setLoggedInAccount(account);
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Sai tên đăng nhập hoặc mật khẩu!");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể kết nối tới máy chủ!");
                    e.printStackTrace();
                }
            }
        });
    }
    private BorderPane createStatisticsPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createRevenueTab(),
                createProductTab(),
                createExpiryAlertTab(),
                createLowStockAlertTab(),
                createEmployeeTab()
        );

        pane.setCenter(tabPane);
        return pane;
    }

    private Tab createRevenueTab() {
        Tab tab = new Tab("Doanh Thu & Tăng Trưởng");
        tab.setClosable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        DatePicker dpStart = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker dpEnd = new DatePicker(LocalDate.now());
        Button btnView = new Button("Xem Thống Kê");
        btnView.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button btnToday = new Button("Hôm nay");
        Button btnMonth = new Button("Tháng này");
        Button btnYear = new Button("Năm này");
        
        btnToday.setOnAction(e -> { dpStart.setValue(LocalDate.now()); dpEnd.setValue(LocalDate.now()); btnView.fire(); });
        btnMonth.setOnAction(e -> { dpStart.setValue(LocalDate.now().withDayOfMonth(1)); dpEnd.setValue(LocalDate.now()); btnView.fire(); });
        btnYear.setOnAction(e -> { dpStart.setValue(LocalDate.now().withDayOfYear(1)); dpEnd.setValue(LocalDate.now()); btnView.fire(); });

        Label lblTotalRevenue = new Label("Tổng doanh thu: 0 VNĐ");
        lblTotalRevenue.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        filterBox.getChildren().addAll(new Label("Từ:"), dpStart, new Label("Đến:"), dpEnd, btnView, btnToday, btnMonth, btnYear, lblTotalRevenue);

        CategoryAxis xAxis = new CategoryAxis(); xAxis.setLabel("Ngày");
        xAxis.setTickLabelRotation(-45);
        NumberAxis yAxis = new NumberAxis(); yAxis.setLabel("Doanh Thu (VNĐ)");
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Biểu đồ tăng trưởng doanh thu");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        lineChart.getData().add(series);

        btnView.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                try {
                    List<Object[]> data = RMICLientFactory.getThongKeService().getDoanhThuTheoNgay(dpStart.getValue(), dpEnd.getValue());
                    Platform.runLater(() -> {
                        series.getData().clear();
                        double total = 0;
                        for (Object[] row : data) {
                            double val = Double.parseDouble(row[1].toString());
                            series.getData().add(new XYChart.Data<>(row[0].toString(), val));
                            total += val;
                        }
                        lblTotalRevenue.setText(String.format("Tổng doanh thu: %,.0f VNĐ", total));
                    });
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        layout.getChildren().addAll(filterBox, lineChart);
        tab.setContent(layout);
        return tab;
    }

    private Tab createProductTab() {
        Tab tab = new Tab("Sản Phẩm Bán Chạy");
        tab.setClosable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        // Trục X là Số lượng, Trục Y là Tên sản phẩm để tạo biểu đồ ngang
        NumberAxis xAxis = new NumberAxis(); xAxis.setLabel("Số lượng bán");
        CategoryAxis yAxis = new CategoryAxis(); yAxis.setLabel("Sản phẩm");
        yAxis.setSide(Side.LEFT);
        yAxis.setTickLabelGap(10);
        yAxis.setTickLabelRotation(0);
        yAxis.setTickLabelFont(Font.font("Arial", 12));
        
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Top 10 sản phẩm bán chạy");
        barChart.setLegendVisible(false); // Ẩn chú thích cho gọn
        barChart.setCategoryGap(18);
        barChart.setBarGap(4);
        barChart.setAnimated(false);
        barChart.setMinHeight(620);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        barChart.getData().add(series);

        Button btnRefresh = new Button("Làm mới dữ liệu");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefresh.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                try {
                    List<Object[]> data = RMICLientFactory.getThongKeService().getTopSanPhamBanChay(LocalDate.now().minusYears(1), LocalDate.now(), 10);
                    Platform.runLater(() -> {
                        series.getData().clear();
                        // 1 category / 1 thuốc, có đánh số + rút gọn để không đè nhãn.
                        int rank = 1;
                        // Thêm dữ liệu ngược lại để sản phẩm bán chạy nhất nằm trên cùng
                        for (int i = data.size() - 1; i >= 0; i--) {
                            Object[] row = data.get(i);
                            String fullName = row[0] == null ? "" : row[0].toString();
                            String label = rank + ". " + shortenLabel(fullName, 26);
                            XYChart.Data<Number, String> chartData = new XYChart.Data<>((Number) row[1], label);
                            chartData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                                if (newNode != null) {
                                    Tooltip.install(newNode, new Tooltip(fullName));
                                }
                            });
                            series.getData().add(chartData);
                            rank++;
                        }
                        // Tăng chiều cao theo số item để mỗi nhãn có 1 "ô" riêng.
                        barChart.setPrefHeight(Math.max(620, data.size() * 58 + 180));
                    });
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        layout.getChildren().addAll(btnRefresh, barChart);
        VBox.setVgrow(barChart, Priority.ALWAYS);
        tab.setContent(layout);
        return tab;
    }

    private String shortenLabel(String text, int maxLen) {
        if (text == null) return "";
        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLen) return normalized;
        return normalized.substring(0, Math.max(0, maxLen - 1)) + "…";
    }

    private boolean isExpiryWithinMonths(SanPham sp, int months) {
        if (sp == null || sp.getHanSD() == null) return false;
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusMonths(months);
        return !sp.getHanSD().isBefore(now) && !sp.getHanSD().isAfter(threshold);
    }

    private boolean isLowStock(SanPham sp) {
        if (sp == null) return false;
        int threshold = sp.getNguongTonToiThieu();
        return sp.getSoLuongTon() < Math.max(0, threshold);
    }

    private boolean isProductEnabledForSale(SanPham sp) {
        if (sp == null || sp.getTinhTrangSP() == null) return false;
        return sp.getTinhTrangSP() == ETinhTrangSP.CON_HANG;
    }

    private void toggleProductSaleStatus(SanPham sp) {
        ETinhTrangSP next = isProductEnabledForSale(sp) ? ETinhTrangSP.HET_HANG : ETinhTrangSP.CON_HANG;
        sp.setTinhTrangSP(next);
        try {
            boolean ok = RMICLientFactory.getSanPhamService().update(sp);
            if (ok) {
                loadDataAsync();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái ON/OFF của sản phẩm.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái ON/OFF: " + ex.getMessage());
        }
    }

    private String buildProductAlertNote(SanPham sp) {
        if (sp == null) return "";
        List<String> notes = new ArrayList<>();
        if (!isProductEnabledForSale(sp)) {
            notes.add("Đang tắt bán");
        }
        if (sp.getHanSD() != null && sp.getHanSD().isBefore(LocalDate.now())) {
            notes.add("Sản phẩm hết hạn");
        } else if (isExpiryWithinMonths(sp, 3)) {
            notes.add("Sắp hết hạn (< 3 tháng)");
        } else if (isExpiryWithinMonths(sp, 6)) {
            notes.add("Sắp hết hạn (< 6 tháng)");
        }
        if (isLowStock(sp)) {
            notes.add("Số lượng tồn thấp");
        }
        return String.join(" | ", notes);
    }

    private Tab createExpiryAlertTab() {
        Tab tab = new Tab("Cảnh Báo Hạn Dùng");
        tab.setClosable(false);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        TableView<SanPham> table = new TableView<>();
        ObservableList<SanPham> data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã SP");
        colMa.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        colMa.setPrefWidth(170);
        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên Sản Phẩm");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colTen.setPrefWidth(290);
        TableColumn<SanPham, LocalDate> colHan = new TableColumn<>("Hạn Sử Dụng");
        colHan.setCellValueFactory(new PropertyValueFactory<>("hanSD"));
        colHan.setPrefWidth(150);
        TableColumn<SanPham, Integer> colConLai = new TableColumn<>("Số Ngày Còn Lại");
        colConLai.setCellValueFactory(c -> {
            LocalDate hsd = c.getValue().getHanSD();
            long days = (hsd == null) ? 0 : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), hsd);
            return new javafx.beans.property.SimpleObjectProperty<>((int) days);
        });
        colConLai.setPrefWidth(160);
        table.getColumns().addAll(colMa, colTen, colHan, colConLai);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null) {
                    setStyle("");
                } else if (isExpiryWithinMonths(sp, 3)) {
                    setStyle("-fx-background-color: #fdeaea;");
                } else {
                    setStyle("-fx-background-color: #fff6e5;");
                }
            }
        });

        ComboBox<String> cbRange = new ComboBox<>();
        cbRange.getItems().addAll("Dưới 3 tháng", "Dưới 6 tháng");
        cbRange.setValue("Dưới 3 tháng");
        cbRange.setPrefWidth(150);

        Label lblCount = new Label("Số sản phẩm cảnh báo: 0");
        lblCount.setStyle("-fx-font-weight: bold;");

        Runnable refresh = () -> {
            int months = "Dưới 6 tháng".equals(cbRange.getValue()) ? 6 : 3;
            List<SanPham> matched = productList.stream()
                    .filter(sp -> isExpiryWithinMonths(sp, months))
                    .sorted((a, b) -> {
                        LocalDate d1 = a.getHanSD();
                        LocalDate d2 = b.getHanSD();
                        if (d1 == null && d2 == null) return 0;
                        if (d1 == null) return 1;
                        if (d2 == null) return -1;
                        return d1.compareTo(d2);
                    })
                    .toList();
            data.setAll(matched);
            lblCount.setText("Số sản phẩm cảnh báo: " + matched.size());
        };

        cbRange.setOnAction(e -> refresh.run());
        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setOnAction(e -> {
            loadDataAsync();
            refresh.run();
        });

        HBox tools = new HBox(10, new Label("Ngưỡng:"), cbRange, btnRefresh, lblCount);
        tools.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(tools, table);
        tab.setContent(layout);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) refresh.run();
        });
        return tab;
    }

    private Tab createLowStockAlertTab() {
        Tab tab = new Tab("Cảnh Báo Tồn Kho Thấp");
        tab.setClosable(false);

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        TableView<SanPham> table = new TableView<>();
        ObservableList<SanPham> data = FXCollections.observableArrayList();
        table.setItems(data);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã SP");
        colMa.setCellValueFactory(new PropertyValueFactory<>("maSP"));
        colMa.setPrefWidth(170);
        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên Sản Phẩm");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenSP"));
        colTen.setPrefWidth(300);
        TableColumn<SanPham, Integer> colTon = new TableColumn<>("Tồn Hiện Tại");
        colTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colTon.setPrefWidth(130);
        TableColumn<SanPham, Integer> colMin = new TableColumn<>("Ngưỡng Tối Thiểu");
        colMin.setCellValueFactory(new PropertyValueFactory<>("nguongTonToiThieu"));
        colMin.setPrefWidth(150);
        TableColumn<SanPham, Integer> colThieu = new TableColumn<>("Thiếu Hụt");
        colThieu.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                Math.max(0, c.getValue().getNguongTonToiThieu() - c.getValue().getSoLuongTon())
        ));
        colThieu.setPrefWidth(110);
        table.getColumns().addAll(colMa, colTen, colTon, colMin, colThieu);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SanPham sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty || sp == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: #fdeaea;");
                }
            }
        });

        Label lblCount = new Label("Số sản phẩm tồn thấp: 0");
        lblCount.setStyle("-fx-font-weight: bold;");

        Runnable refresh = () -> {
            List<SanPham> matched = productList.stream()
                    .filter(this::isLowStock)
                    .sorted((a, b) -> Integer.compare(
                            Math.max(0, b.getNguongTonToiThieu() - b.getSoLuongTon()),
                            Math.max(0, a.getNguongTonToiThieu() - a.getSoLuongTon())
                    ))
                    .toList();
            data.setAll(matched);
            lblCount.setText("Số sản phẩm tồn thấp: " + matched.size());
        };

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setOnAction(e -> {
            loadDataAsync();
            refresh.run();
        });

        HBox tools = new HBox(10, btnRefresh, lblCount);
        tools.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(tools, table);
        tab.setContent(layout);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) refresh.run();
        });
        return tab;
    }

    private Tab createEmployeeTab() {
        Tab tab = new Tab("Hiệu Suất Nhân Viên");
        tab.setClosable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        TableView<Object[]> table = new TableView<>();
        TableColumn<Object[], String> colName = new TableColumn<>("Nhân Viên");
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0].toString()));
        
        TableColumn<Object[], String> colCount = new TableColumn<>("Số Hóa Đơn");
        colCount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1].toString()));

        TableColumn<Object[], String> colRevenue = new TableColumn<>("Tổng Doanh Số");
        colRevenue.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f VNĐ", Double.parseDouble(data.getValue()[2].toString()))));

        table.getColumns().addAll(colName, colCount, colRevenue);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button btnRefresh = new Button("Thống kê hiệu suất");
        btnRefresh.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                try {
                    List<Object[]> data = RMICLientFactory.getThongKeService().getHieuSuatNhanVien(LocalDate.now().minusYears(1), LocalDate.now());
                    Platform.runLater(() -> table.getItems().setAll(data));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        layout.getChildren().addAll(btnRefresh, table);
        tab.setContent(layout);
        return tab;
    }

    private BorderPane createImportManagementPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15));

        Label title = new Label("📦 Quản Lý Nhập Hàng");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        pane.setTop(title);

        TableView<PhieuNhap> table = new TableView<>();
        TableColumn<PhieuNhap, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(new PropertyValueFactory<>("maPhieuNhap"));
        TableColumn<PhieuNhap, String> colNgay = new TableColumn<>("Ngày Nhập");
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngayNhap"));
        TableColumn<PhieuNhap, String> colNCC = new TableColumn<>("Nhà Cung Cấp");
        colNCC.setCellValueFactory(new PropertyValueFactory<>("nhaCungCap"));
        TableColumn<PhieuNhap, String> colCN = new TableColumn<>("Chi Nhánh");
        colCN.setCellValueFactory(new PropertyValueFactory<>("chiNhanh"));
        TableColumn<PhieuNhap, Double> colTotal = new TableColumn<>("Tổng Tiền");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("tongTien"));
        formatCurrencyColumn(colTotal);

        table.getColumns().addAll(colMa, colNgay, colNCC, colCN, colTotal);
        pane.setCenter(table);

        Button btnNew = new Button("➕ Nhập Hàng Mới");
        btnNew.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNew.setOnAction(e -> {
            NhanVien currentNV = (loggedInAccount != null) ? loggedInAccount.getNhanVien() : null;
            if (currentNV == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên đang đăng nhập!");
                return;
            }
            ImportDialog dialog = new ImportDialog(this, currentNV);
            dialog.showAndWait();
            // Refresh table after dialog closes
            refreshImportList(table);
        });

        Button btnViewDetail = new Button("👁️ Xem Chi Tiết");
        btnViewDetail.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnViewDetail.setOnAction(e -> {
            PhieuNhap selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn phiếu", "Vui lòng chọn phiếu nhập cần xem!");
                return;
            }
            showImportDetails(selected);
        });

        HBox footer = new HBox(15, btnNew, btnViewDetail);
        footer.setPadding(new Insets(15, 0, 0, 0));
        pane.setBottom(footer);

        CompletableFuture.runAsync(() -> refreshImportList(table));

        return pane;
    }

    private void refreshImportList(TableView<PhieuNhap> table) {
        CompletableFuture.runAsync(() -> {
            try {
                List<PhieuNhap> list = RMICLientFactory.getPhieuNhapService().getAll();
                Platform.runLater(() -> {
                    if (loggedInAccount != null && loggedInAccount.getVaiTro() != VaiTro.ADMIN
                            && loggedInAccount.getNhanVien() != null) {
                        String myBranch = loggedInAccount.getNhanVien().getChiNhanh();
                        table.getItems().setAll(list.stream()
                                .filter(p -> myBranch != null && p.getChiNhanh() != null 
                                        && myBranch.trim().equalsIgnoreCase(p.getChiNhanh().trim()))
                                .toList());
                    } else {
                        table.getItems().setAll(list);
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void refreshExportList(TableView<PhieuXuat> table) {
        CompletableFuture.runAsync(() -> {
            try {
                List<PhieuXuat> list = RMICLientFactory.getPhieuXuatService().getAll();
                Platform.runLater(() -> {
                    if (loggedInAccount != null && loggedInAccount.getVaiTro() != VaiTro.ADMIN
                            && loggedInAccount.getNhanVien() != null) {
                        String myBranch = loggedInAccount.getNhanVien().getChiNhanh();
                        table.getItems().setAll(list.stream()
                                .filter(p -> myBranch != null && p.getChiNhanh() != null 
                                        && myBranch.trim().equalsIgnoreCase(p.getChiNhanh().trim()))
                                .toList());
                    } else {
                        table.getItems().setAll(list);
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private BorderPane createExportManagementPane() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15));

        Label title = new Label("📤 Quản Lý Xuất Kho");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");
        pane.setTop(title);

        TableView<PhieuXuat> table = new TableView<>();
        TableColumn<PhieuXuat, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(new PropertyValueFactory<>("maPhieuXuat"));
        TableColumn<PhieuXuat, String> colNgay = new TableColumn<>("Ngày Xuất");
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngayXuat"));
        TableColumn<PhieuXuat, String> colLyDo = new TableColumn<>("Lý Do");
        colLyDo.setCellValueFactory(new PropertyValueFactory<>("lyDoXuat"));
        TableColumn<PhieuXuat, String> colCN = new TableColumn<>("Chi Nhánh");
        colCN.setCellValueFactory(new PropertyValueFactory<>("chiNhanh"));

        table.getColumns().addAll(colMa, colNgay, colLyDo, colCN);
        pane.setCenter(table);

        Button btnNew = new Button("➕ Lập Phiếu Xuất");
        btnNew.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNew.setOnAction(e -> {
            NhanVien currentNV = (loggedInAccount != null) ? loggedInAccount.getNhanVien() : null;
            if (currentNV == null) return;
            ExportDialog dialog = new ExportDialog(currentNV);
            dialog.showAndWait();
            refreshExportList(table);
        });

        Button btnViewDetail = new Button("👁️ Xem Chi Tiết");
        btnViewDetail.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnViewDetail.setOnAction(e -> {
            PhieuXuat selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn phiếu", "Vui lòng chọn phiếu xuất cần xem!");
                return;
            }
            showExportDetails(selected);
        });

        HBox footer = new HBox(15, btnNew, btnViewDetail);
        footer.setPadding(new Insets(15, 0, 0, 0));
        pane.setBottom(footer);

        CompletableFuture.runAsync(() -> refreshExportList(table));

        return pane;
    }

    private void showImportDetails(PhieuNhap pn) {
        CompletableFuture.runAsync(() -> {
            try {
                PhieuNhap fullPn = RMICLientFactory.getPhieuNhapService().findByIdWithDetails(pn.getMaPhieuNhap());
                Platform.runLater(() -> {
                    if (fullPn == null) return;
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Chi tiết phiếu nhập: " + fullPn.getMaPhieuNhap());
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

                    VBox root = new VBox(10);
                    root.setPadding(new Insets(15));
                    root.setPrefWidth(600);

                    GridPane header = new GridPane();
                    header.setHgap(10); header.setVgap(5);
                    header.add(new Label("Ngày nhập:"), 0, 0); header.add(new Label(fullPn.getNgayNhap().toString()), 1, 0);
                    header.add(new Label("Nhà cung cấp:"), 0, 1); header.add(new Label(fullPn.getNhaCungCap()), 1, 1);
                    header.add(new Label("Nhân viên:"), 0, 2); header.add(new Label(fullPn.getNhanVien() != null ? fullPn.getNhanVien().getHoTen() : ""), 1, 2);
                    header.add(new Label("Tổng tiền:"), 0, 3); 
                    Label lblTotal = new Label(String.format("%,.0f VNĐ", fullPn.getTongTien()));
                    lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");
                    header.add(lblTotal, 1, 3);

                    TableView<ChiTietPhieuNhap> table = new TableView<>();
                    TableColumn<ChiTietPhieuNhap, String> colSP = new TableColumn<>("Sản Phẩm");
                    colSP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSanPham().getTenSP()));
                    TableColumn<ChiTietPhieuNhap, Integer> colSL = new TableColumn<>("Số Lượng");
                    colSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
                    TableColumn<ChiTietPhieuNhap, Double> colGia = new TableColumn<>("Giá Nhập");
                    colGia.setCellValueFactory(new PropertyValueFactory<>("giaNhap"));
                    formatCurrencyColumn(colGia);
                    TableColumn<ChiTietPhieuNhap, String> colExp = new TableColumn<>("Hạn Sử Dụng");
                    colExp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNgayHetHan().toString()));

                    table.getColumns().addAll(colSP, colSL, colGia, colExp);
                    table.getItems().setAll(fullPn.getChiTietPhieuNhaps());
                    table.setPrefHeight(300);

                    root.getChildren().addAll(header, new Separator(), new Label("DANH SÁCH MẶT HÀNG:"), table);
                    dialog.getDialogPane().setContent(root);
                    dialog.showAndWait();
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void showExportDetails(PhieuXuat px) {
        CompletableFuture.runAsync(() -> {
            try {
                PhieuXuat fullPx = RMICLientFactory.getPhieuXuatService().findByIdWithDetails(px.getMaPhieuXuat());
                Platform.runLater(() -> {
                    if (fullPx == null) return;
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Chi tiết phiếu xuất: " + fullPx.getMaPhieuXuat());
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

                    VBox root = new VBox(10);
                    root.setPadding(new Insets(15));
                    root.setPrefWidth(500);

                    GridPane header = new GridPane();
                    header.setHgap(10); header.setVgap(5);
                    header.add(new Label("Ngày xuất:"), 0, 0); header.add(new Label(fullPx.getNgayXuat().toString()), 1, 0);
                    header.add(new Label("Lý do:"), 0, 1); header.add(new Label(fullPx.getLyDoXuat()), 1, 1);
                    header.add(new Label("Nhân viên:"), 0, 2); header.add(new Label(fullPx.getNhanVien() != null ? fullPx.getNhanVien().getHoTen() : ""), 1, 2);

                    TableView<ChiTietPhieuXuat> table = new TableView<>();
                    TableColumn<ChiTietPhieuXuat, String> colSP = new TableColumn<>("Sản Phẩm");
                    colSP.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSanPham().getTenSP()));
                    TableColumn<ChiTietPhieuXuat, Integer> colSL = new TableColumn<>("Số Lượng");
                    colSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));

                    table.getColumns().addAll(colSP, colSL);
                    table.getItems().setAll(fullPx.getChiTietPhieuXuats());
                    table.setPrefHeight(300);

                    root.getChildren().addAll(header, new Separator(), new Label("DANH SÁCH MẶT HÀNG:"), table);
                    dialog.getDialogPane().setContent(root);
                    dialog.showAndWait();
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void showChangePasswordDialog() {
        if (loggedInAccount == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa đăng nhập", "Vui lòng đăng nhập trước khi đổi mật khẩu.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Thay đổi mật khẩu cho tài khoản: " + loggedInAccount.getMaTaiKhoan());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        PasswordField txtOld = new PasswordField();
        PasswordField txtNew = new PasswordField();
        PasswordField txtConfirm = new PasswordField();

        grid.add(new Label("Mật khẩu cũ:"), 0, 0);
        grid.add(txtOld, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(txtNew, 1, 1);
        grid.add(new Label("Nhập lại mật khẩu mới:"), 0, 2);
        grid.add(txtConfirm, 1, 2);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String oldPass = txtOld.getText();
                String newPass = txtNew.getText();
                String confirmPass = txtConfirm.getText();

                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới không khớp.");
                    return;
                }

                try {
                    boolean success = RMICLientFactory.getAccountService().changePassword(loggedInAccount.getMaTaiKhoan(), oldPass, newPass);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu cũ không chính xác.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Lỗi: " + ex.getMessage());
                }
            }
        });
    }
}
