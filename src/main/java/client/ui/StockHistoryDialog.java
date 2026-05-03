package client.ui;

import client.RMICLientFactory;
import entities.ChiTietPhieuNhap;
import entities.ChiTietPhieuXuat;
import entities.SanPham;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class StockHistoryDialog extends Stage {
    private String branch;

    public StockHistoryDialog(SanPham sp, String branch) {
        this.branch = branch;
        setTitle("Lịch sử nhập xuất: " + sp.getTenSP() + (branch != null ? " [" + branch + "]" : " [Kho tổng]"));
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        Label lblTitle = new Label("Sản phẩm: " + sp.getTenSP() + " - Đang tải...");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TabPane tabPane = new TabPane();

        // Tab Nhập hàng
        Tab tabNhap = new Tab("Lịch sử Nhập hàng");
        tabNhap.setClosable(false);
        TableView<ChiTietPhieuNhap> tableNhap = new TableView<>();
        TableColumn<ChiTietPhieuNhap, LocalDate> colNgayNhap = new TableColumn<>("Ngày Nhập");
        colNgayNhap.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().getPhieuNhap().getNgayNhap()));
        TableColumn<ChiTietPhieuNhap, Integer> colQtyNhap = new TableColumn<>("Số Lượng");
        colQtyNhap.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        TableColumn<ChiTietPhieuNhap, LocalDate> colHSD = new TableColumn<>("Hạn Sử Dụng");
        colHSD.setCellValueFactory(new PropertyValueFactory<>("ngayHetHan"));
        tableNhap.getColumns().addAll(colNgayNhap, colQtyNhap, colHSD);
        tabNhap.setContent(tableNhap);

        // Tab Xuất hàng
        Tab tabXuat = new Tab("Lịch sử Xuất kho");
        tabXuat.setClosable(false);
        TableView<ChiTietPhieuXuat> tableXuat = new TableView<>();
        TableColumn<ChiTietPhieuXuat, LocalDate> colNgayXuat = new TableColumn<>("Ngày Xuất");
        colNgayXuat.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().getPhieuXuat().getNgayXuat()));
        TableColumn<ChiTietPhieuXuat, Integer> colQtyXuat = new TableColumn<>("Số Lượng");
        colQtyXuat.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        TableColumn<ChiTietPhieuXuat, String> colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(data -> javafx.beans.binding.Bindings.createStringBinding(() -> data.getValue().getPhieuXuat().getLyDoXuat()));
        tableXuat.getColumns().addAll(colNgayXuat, colQtyXuat, colLyDo);
        tabXuat.setContent(tableXuat);

        // Tab Bán lẻ
        Tab tabBanLe = new Tab("Lịch sử Bán lẻ");
        tabBanLe.setClosable(false);
        TableView<entities.ChiTietHoaDon> tableBanLe = new TableView<>();
        TableColumn<entities.ChiTietHoaDon, LocalDate> colNgayBan = new TableColumn<>("Ngày Bán");
        colNgayBan.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().getHoaDon().getNgayLapHD()));
        TableColumn<entities.ChiTietHoaDon, Integer> colQtyBan = new TableColumn<>("Số Lượng");
        colQtyBan.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        tableBanLe.getColumns().addAll(colNgayBan, colQtyBan);
        tabBanLe.setContent(tableBanLe);

        tabPane.getTabs().addAll(tabNhap, tabBanLe, tabXuat);
        root.getChildren().addAll(lblTitle, tabPane);

        // Load data
        try {
            List<ChiTietPhieuNhap> nhaps = RMICLientFactory.getPhieuNhapService().getDetailsByProduct(sp.getMaSP());
            List<ChiTietPhieuXuat> xuats = RMICLientFactory.getPhieuXuatService().getDetailsByProduct(sp.getMaSP());
            List<entities.ChiTietHoaDon> banles = RMICLientFactory.getHoaDonService().getDetailsByProduct(sp.getMaSP());

            if (branch != null) {
                nhaps = nhaps.stream().filter(ct -> branch.equals(ct.getPhieuNhap().getChiNhanh())).toList();
                xuats = xuats.stream().filter(ct -> branch.equals(ct.getPhieuXuat().getChiNhanh())).toList();
                banles = banles.stream().filter(ct -> branch.equals(ct.getHoaDon().getChiNhanh())).toList();
            } else {
                nhaps = nhaps.stream().filter(ct -> ct.getPhieuNhap().getChiNhanh() == null).toList();
                xuats = xuats.stream().filter(ct -> ct.getPhieuXuat().getChiNhanh() == null).toList();
                banles = banles.stream().filter(ct -> ct.getHoaDon().getChiNhanh() == null).toList();
            }

            tableNhap.setItems(FXCollections.observableArrayList(nhaps));
            tableXuat.setItems(FXCollections.observableArrayList(xuats));
            tableBanLe.setItems(FXCollections.observableArrayList(banles));

            int tongNhap = nhaps.stream().mapToInt(ChiTietPhieuNhap::getSoLuong).sum();
            int tongBanLe = banles.stream().mapToInt(entities.ChiTietHoaDon::getSoLuong).sum();
            int tongXuat = xuats.stream().mapToInt(ChiTietPhieuXuat::getSoLuong).sum();
            int hienTon = Math.max(0, tongNhap - tongBanLe - tongXuat);

            lblTitle.setText("Sản phẩm: " + sp.getTenSP() + " - Hiện tồn: " + hienTon
                    + " (Nhập: " + tongNhap + ", Bán: " + tongBanLe + ", Xuất: " + tongXuat + ")");

        } catch (Exception e) { e.printStackTrace(); }

        Scene scene = new Scene(root, 750, 550);
        setScene(scene);
    }
}
