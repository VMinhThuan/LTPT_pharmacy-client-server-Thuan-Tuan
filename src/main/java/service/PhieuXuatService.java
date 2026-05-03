package service;

import entities.PhieuXuat;
import java.rmi.RemoteException;
import java.util.List;

public interface PhieuXuatService extends GenericService<PhieuXuat, String> {
    List<entities.ChiTietPhieuXuat> getDetailsByProduct(String maSP) throws RemoteException;
    PhieuXuat findByIdWithDetails(String id) throws RemoteException;
}
