package entities.enums;

import lombok.Getter;

@Getter
public enum ELoaiSanPham {
    THUOC("thuốc"), THUC_PHAM("thực phẩm chức năng");

    private final String loaiSP;
    ELoaiSanPham(String loaiSP) {
        this.loaiSP = loaiSP;
    }
}
