package service.impl;

import dao.ThongKeDAO;
import service.ThongKeService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

public class ThongKeServiceImpl extends UnicastRemoteObject implements ThongKeService {
    private ThongKeDAO thongKeDAO;

    public ThongKeServiceImpl(ThongKeDAO thongKeDAO) throws RemoteException {
        this.thongKeDAO = thongKeDAO;
    }

    public ThongKeServiceImpl() throws RemoteException {
        this(new ThongKeDAO());
    }

    @Override
    public List<Object[]> getDoanhThuTheoNgay(LocalDate start, LocalDate end) throws RemoteException {
        return thongKeDAO.getDoanhThuTheoNgay(start, end);
    }

    @Override
    public List<Object[]> getTopSanPhamBanChay(LocalDate start, LocalDate end, int limit) throws RemoteException {
        return thongKeDAO.getTopSanPhamBanChay(start, end, limit);
    }

    @Override
    public List<Object[]> getHieuSuatNhanVien(LocalDate start, LocalDate end) throws RemoteException {
        return thongKeDAO.getHieuSuatNhanVien(start, end);
    }
}
