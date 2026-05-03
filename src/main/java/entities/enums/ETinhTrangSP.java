package entities.enums;

import lombok.Getter;

@Getter
public enum ETinhTrangSP {
    CON_HANG("còn hàng"), HET_HANG("hết hàng"), SAP_CO_HANG("sắp có hàng");

    private final String tinhTrang;
    ETinhTrangSP(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }
}
