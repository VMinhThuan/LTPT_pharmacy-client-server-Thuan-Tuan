package service;

import entities.PhieuNhap;
import java.rmi.RemoteException;
import java.util.List;

public interface PhieuNhapService extends GenericService<PhieuNhap, String> {
    List<entities.ChiTietPhieuNhap> getDetailsByProduct(String maSP) throws RemoteException;
    List<entities.ChiTietPhieuNhap> getDetailsByProductAndBranch(String maSP, String chiNhanh) throws RemoteException;
    PhieuNhap findByIdWithDetails(String id) throws RemoteException;
}
