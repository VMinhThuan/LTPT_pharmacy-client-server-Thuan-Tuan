package client.ui;

import entities.SanPham;
import javafx.beans.property.*;

public class CartItem {
    private ObjectProperty<SanPham> sanPham;
    private IntegerProperty soLuong;
    private DoubleProperty donGia;
    private DoubleProperty thanhTien;

    public CartItem(SanPham sanPham, int soLuong) {
        this.sanPham = new SimpleObjectProperty<>(sanPham);
        this.soLuong = new SimpleIntegerProperty(soLuong);
        this.donGia = new SimpleDoubleProperty(sanPham.getGiaBan());
        this.thanhTien = new SimpleDoubleProperty(sanPham.getGiaBan() * soLuong);

        // Auto update thanhTien when soLuong changes
        this.soLuong.addListener((obs, oldVal, newVal) -> {
            this.thanhTien.set(sanPham.getGiaBan() * newVal.intValue());
        });
    }

    public SanPham getSanPham() { return sanPham.get(); }
    public String getTenSP() { return sanPham.get().getTenSP(); }
    public int getSoLuong() { return soLuong.get(); }
    public void setSoLuong(int soLuong) { this.soLuong.set(soLuong); }
    public IntegerProperty soLuongProperty() { return soLuong; }
    public double getDonGia() { return donGia.get(); }
    public double getThanhTien() { return thanhTien.get(); }
    public DoubleProperty thanhTienProperty() { return thanhTien; }
}
