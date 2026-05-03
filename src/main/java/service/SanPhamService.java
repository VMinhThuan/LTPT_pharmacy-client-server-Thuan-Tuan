package service;

import entities.SanPham;

public interface SanPhamService extends GenericService<SanPham, String> {
    java.util.List<SanPham> getAllByChiNhanh(String chiNhanh) throws Exception;
}
