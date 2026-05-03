package service.impl;

import dao.ChiTietHoaDonDAO;
import entities.ChiTietHoaDon;
import service.ChiTietHoaDonService;

import java.rmi.RemoteException;

public class ChiTietHoaDonServiceImpl extends GenericServiceImpl<ChiTietHoaDon, Long> implements ChiTietHoaDonService {
    public ChiTietHoaDonServiceImpl() throws RemoteException {
        super(new ChiTietHoaDonDAO());
    }
}
