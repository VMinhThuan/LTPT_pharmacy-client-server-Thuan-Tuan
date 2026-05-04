package service.impl;

import config.MariaDBConnection;
import dao.SanPhamDAO;
import entities.DonViTinh;
import entities.NhomThuoc;
import entities.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import service.SanPhamService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SanPhamServiceImpl extends GenericServiceImpl<SanPham, String> implements SanPhamService {
    public SanPhamServiceImpl() throws RemoteException {
        super(new SanPhamDAO());
    }

    @Override
    public List<SanPham> getAll() throws Exception {
        recalculateStockFromHistory();
        List<SanPham> raw = super.getAll();
        List<SanPham> safe = new ArrayList<>(raw.size());
        for (SanPham sp : raw) {
            safe.add(toSafeSanPham(sp));
        }
        return safe;
    }

    @Override
    public List<SanPham> getAllByChiNhanh(String chiNhanh) throws Exception {
        recalculateStockFromHistory();
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            List<SanPham> raw = em.createQuery("SELECT s FROM SanPham s", SanPham.class).getResultList();
            Map<String, Integer> tonTheoChiNhanh = computeStockByBranch(em, chiNhanh);
            List<SanPham> safe = new ArrayList<>(raw.size());
            for (SanPham sp : raw) {
                SanPham copy = toSafeSanPham(sp);
                copy.setSoLuongTon(Math.max(0, tonTheoChiNhanh.getOrDefault(sp.getMaSP(), 0)));
                safe.add(copy);
            }
            return safe;
        } finally {
            em.close();
        }
    }

    @Override
    public SanPham find(String id) throws Exception {
        SanPham sp = super.find(id);
        return sp == null ? null : toSafeSanPham(sp);
    }

    private SanPham toSafeSanPham(SanPham src) {
        SanPham dst = new SanPham();
        dst.setMaSP(src.getMaSP());
        dst.setTenSP(src.getTenSP());
        dst.setNgaySX(src.getNgaySX());
        dst.setHanSD(src.getHanSD());
        dst.setKhoiLuong(src.getKhoiLuong());
        dst.setMoTa(src.getMoTa());
        dst.setTinhTrangSP(src.getTinhTrangSP());
        dst.setNuocSX(src.getNuocSX());
        dst.setThuongHieu(src.getThuongHieu());
        dst.setGiaBan(src.getGiaBan());
        dst.setLoaiSP(src.getLoaiSP());
        dst.setTacDungPhu(src.getTacDungPhu());
        dst.setThuocKeDon(src.isThuocKeDon());
        dst.setLoiKhuyen(src.getLoiKhuyen());
        dst.setCongDung(src.getCongDung());
        dst.setSoLuongTon(src.getSoLuongTon());
        dst.setNguongTonToiThieu(src.getNguongTonToiThieu());

        if (src.getDonViTinh() != null) {
            DonViTinh dvt = new DonViTinh();
            dvt.setMaDonViTinh(src.getDonViTinh().getMaDonViTinh());
            dvt.setTenDonViTinh(src.getDonViTinh().getTenDonViTinh());
            dst.setDonViTinh(dvt);
        }

        if (src.getNhomThuoc() != null) {
            NhomThuoc nt = new NhomThuoc();
            nt.setMaNhomThuoc(src.getNhomThuoc().getMaNhomThuoc());
            nt.setTenNhomThuoc(src.getNhomThuoc().getTenNhomThuoc());
            dst.setNhomThuoc(nt);
        }

        return dst;
    }

    /**
     * Đồng bộ tồn kho theo dữ liệu gốc:
     * soLuongTon = tổng nhập - tổng bán lẻ - tổng xuất kho
     */
    private void recalculateStockFromHistory() {
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            Map<String, Integer> tongNhap = toQtyMap(em.createQuery(
                    "SELECT ct.sanPham.maSP, SUM(ct.soLuong) " +
                            "FROM ChiTietPhieuNhap ct GROUP BY ct.sanPham.maSP",
                    Object[].class).getResultList());

            Map<String, Integer> tongBanLe = toQtyMap(em.createQuery(
                    "SELECT ct.sanPham.maSP, SUM(ct.soLuong) " +
                            "FROM ChiTietHoaDon ct GROUP BY ct.sanPham.maSP",
                    Object[].class).getResultList());

            Map<String, Integer> tongXuatKho = toQtyMap(em.createQuery(
                    "SELECT ct.sanPham.maSP, SUM(ct.soLuong) " +
                            "FROM ChiTietPhieuXuat ct GROUP BY ct.sanPham.maSP",
                    Object[].class).getResultList());

            List<SanPham> products = em.createQuery("SELECT s FROM SanPham s", SanPham.class).getResultList();
            for (SanPham sp : products) {
                int nhap = tongNhap.getOrDefault(sp.getMaSP(), 0);
                int ban = tongBanLe.getOrDefault(sp.getMaSP(), 0);
                int xuat = tongXuatKho.getOrDefault(sp.getMaSP(), 0);
                int computed = Math.max(0, nhap - ban - xuat);
                if (sp.getSoLuongTon() != computed) {
                    sp.setSoLuongTon(computed);
                    em.merge(sp);
                }
            }

            tr.commit();
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            throw new RuntimeException("Lỗi đồng bộ tồn kho theo lịch sử: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private Map<String, Integer> toQtyMap(List<Object[]> rows) {
        Map<String, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) continue;
            String maSP = row[0].toString();
            int qty = ((Number) row[1]).intValue();
            result.put(maSP, qty);
        }
        return result;
    }

    private Map<String, Integer> computeStockByBranch(EntityManager em, String chiNhanh) {
        String branchWhere;
        if (chiNhanh == null || chiNhanh.equalsIgnoreCase("Kho tổng") || chiNhanh.equalsIgnoreCase("Trụ sở chính")) {
            branchWhere = ""; // No filtering = Global Total
        } else {
            branchWhere = "WHERE LOWER(TRIM(ct.%1$s.chiNhanh)) = LOWER(TRIM(:chiNhanh))";
        }

        TypedQuery<Object[]> qNhap = em.createQuery(
                String.format("SELECT ct.sanPham.maSP, SUM(ct.soLuong) FROM ChiTietPhieuNhap ct %s GROUP BY ct.sanPham.maSP", 
                String.format(branchWhere, "phieuNhap")), Object[].class);
        
        TypedQuery<Object[]> qBan = em.createQuery(
                String.format("SELECT ct.sanPham.maSP, SUM(ct.soLuong) FROM ChiTietHoaDon ct %s GROUP BY ct.sanPham.maSP", 
                String.format(branchWhere, "hoaDon")), Object[].class);
        
        TypedQuery<Object[]> qXuat = em.createQuery(
                String.format("SELECT ct.sanPham.maSP, SUM(ct.soLuong) FROM ChiTietPhieuXuat ct %s GROUP BY ct.sanPham.maSP", 
                String.format(branchWhere, "phieuXuat")), Object[].class);

        if (chiNhanh != null && !chiNhanh.equalsIgnoreCase("Kho tổng")) {
            qNhap.setParameter("chiNhanh", chiNhanh);
            qBan.setParameter("chiNhanh", chiNhanh);
            qXuat.setParameter("chiNhanh", chiNhanh);
        }

        Map<String, Integer> tongNhap = toQtyMap(qNhap.getResultList());
        Map<String, Integer> tongBanLe = toQtyMap(qBan.getResultList());
        Map<String, Integer> tongXuatKho = toQtyMap(qXuat.getResultList());

        Map<String, Integer> result = new HashMap<>();
        // Mix all keys
        java.util.Set<String> allKeys = new java.util.HashSet<>();
        allKeys.addAll(tongNhap.keySet());
        allKeys.addAll(tongBanLe.keySet());
        allKeys.addAll(tongXuatKho.keySet());

        for (String maSP : allKeys) {
            int nhap = tongNhap.getOrDefault(maSP, 0);
            int ban = tongBanLe.getOrDefault(maSP, 0);
            int xuat = tongXuatKho.getOrDefault(maSP, 0);
            result.put(maSP, nhap - ban - xuat);
        }
        return result;
    }
}
