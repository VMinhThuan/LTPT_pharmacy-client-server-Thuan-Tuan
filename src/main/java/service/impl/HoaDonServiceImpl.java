package service.impl;

import dao.HoaDonDAO;
import entities.HoaDon;
import service.HoaDonService;

import java.rmi.RemoteException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import entities.SanPham;

public class HoaDonServiceImpl extends GenericServiceImpl<HoaDon, String> implements HoaDonService {
    public HoaDonServiceImpl() throws RemoteException {
        super(new HoaDonDAO());
    }

    @Override
    public boolean save(HoaDon hd) throws RemoteException {
        EntityManager em = config.MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            String chiNhanh = resolveBranch(hd.getChiNhanh(), hd.getNhanVien());
            hd.setChiNhanh(chiNhanh);
            if (hd.getChiTietHoaDons() != null) {
                for (entities.ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                    SanPham sp = em.find(SanPham.class, ct.getSanPham().getMaSP());
                    if (sp != null) {
                        int branchStock = getBranchStock(em, sp.getMaSP(), chiNhanh);
                        int newStock = branchStock - ct.getSoLuong();
                        if (newStock < 0) throw new Exception("Không đủ hàng trong kho cho SP: " + sp.getTenSP());
                    }
                    ct.setHoaDon(hd);
                }
            }
            em.persist(hd);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public HoaDon findByIdWithDetails(String id) throws RemoteException {
        return ((HoaDonDAO) genericDAO).findByIdWithDetails(id);
    }
    @Override
    public java.util.List<entities.ChiTietHoaDon> getDetailsByProduct(String maSP) throws RemoteException {
        return ((HoaDonDAO) genericDAO).getDetailsByProduct(maSP);
    }

    private String resolveBranch(String chiNhanh, entities.NhanVien nv) {
        if (chiNhanh != null && !chiNhanh.isBlank()) {
            return chiNhanh.trim();
        }
        return (nv != null && nv.getChiNhanh() != null) ? nv.getChiNhanh() : null;
    }

    private int getBranchStock(EntityManager em, String maSP, String chiNhanh) {
        if (chiNhanh == null || chiNhanh.isBlank()) {
            TypedQuery<Long> qNhap = em.createQuery(
                    "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietPhieuNhap ct WHERE ct.sanPham.maSP = :maSP",
                    Long.class);
            TypedQuery<Long> qBan = em.createQuery(
                    "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietHoaDon ct WHERE ct.sanPham.maSP = :maSP",
                    Long.class);
            TypedQuery<Long> qXuat = em.createQuery(
                    "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietPhieuXuat ct WHERE ct.sanPham.maSP = :maSP",
                    Long.class);
            long nhap = qNhap.setParameter("maSP", maSP).getSingleResult();
            long ban = qBan.setParameter("maSP", maSP).getSingleResult();
            long xuat = qXuat.setParameter("maSP", maSP).getSingleResult();
            return (int) Math.max(0, nhap - ban - xuat);
        }

        TypedQuery<Long> qNhap = em.createQuery(
                "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietPhieuNhap ct " +
                        "WHERE ct.sanPham.maSP = :maSP AND ct.phieuNhap.chiNhanh = :chiNhanh",
                Long.class);
        TypedQuery<Long> qBan = em.createQuery(
                "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietHoaDon ct " +
                        "WHERE ct.sanPham.maSP = :maSP AND ct.hoaDon.chiNhanh = :chiNhanh",
                Long.class);
        TypedQuery<Long> qXuat = em.createQuery(
                "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietPhieuXuat ct " +
                        "WHERE ct.sanPham.maSP = :maSP AND ct.phieuXuat.chiNhanh = :chiNhanh",
                Long.class);

        long nhap = qNhap.setParameter("maSP", maSP).setParameter("chiNhanh", chiNhanh).getSingleResult();
        long ban = qBan.setParameter("maSP", maSP).setParameter("chiNhanh", chiNhanh).getSingleResult();
        long xuat = qXuat.setParameter("maSP", maSP).setParameter("chiNhanh", chiNhanh).getSingleResult();
        return (int) Math.max(0, nhap - ban - xuat);
    }
}
