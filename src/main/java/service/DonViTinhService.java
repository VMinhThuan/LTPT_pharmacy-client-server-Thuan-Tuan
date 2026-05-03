package service;

import entities.DonViTinh;

import java.util.List;

public interface DonViTinhService extends GenericService<DonViTinh, String> {
    boolean addProductUnit(DonViTinh donViTinh) throws Exception;
    boolean removeProductUnit(String maDonViTinh) throws Exception;
    boolean updateProductUnit(DonViTinh donViTinh) throws Exception;
    List<DonViTinh> getAllProductUnit() throws Exception;
} 