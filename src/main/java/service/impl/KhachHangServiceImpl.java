package service.impl;

import dao.KhachHangDAO;
import entities.KhachHang;
import service.KhachHangService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class KhachHangServiceImpl extends GenericServiceImpl<KhachHang, String> implements KhachHangService {
    public KhachHangServiceImpl() throws RemoteException {
        super(new KhachHangDAO());
    }

    @Override
    public List<KhachHang> getAll() throws Exception {
        List<KhachHang> raw = super.getAll();
        List<KhachHang> safe = new ArrayList<>(raw.size());
        for (KhachHang kh : raw) safe.add(toSafeKhachHang(kh));
        return safe;
    }

    @Override
    public KhachHang find(String id) throws Exception {
        KhachHang kh = super.find(id);
        return kh == null ? null : toSafeKhachHang(kh);
    }

    private KhachHang toSafeKhachHang(KhachHang src) {
        KhachHang dst = new KhachHang();
        dst.setMaKhachHang(src.getMaKhachHang());
        dst.setHoTen(src.getHoTen());
        dst.setNgaySinh(src.getNgaySinh());
        dst.setGioiTinh(src.isGioiTinh());
        dst.setSoDienThoai(src.getSoDienThoai());
        dst.setNgayThamGia(src.getNgayThamGia());
        dst.setDiemTichLuy(src.getDiemTichLuy());
        return dst;
    }
}
