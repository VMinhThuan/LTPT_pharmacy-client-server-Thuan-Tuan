package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface ThongKeService extends Remote {
    List<Object[]> getDoanhThuTheoNgay(LocalDate start, LocalDate end) throws RemoteException;
    List<Object[]> getTopSanPhamBanChay(LocalDate start, LocalDate end, int limit) throws RemoteException;
    List<Object[]> getHieuSuatNhanVien(LocalDate start, LocalDate end) throws RemoteException;
}
