package service;

import entities.HoaDon;

public interface HoaDonService extends GenericService<HoaDon, String> {
    public HoaDon findByIdWithDetails(String id) throws Exception;
    public java.util.List<entities.ChiTietHoaDon> getDetailsByProduct(String maSP) throws Exception;
}
