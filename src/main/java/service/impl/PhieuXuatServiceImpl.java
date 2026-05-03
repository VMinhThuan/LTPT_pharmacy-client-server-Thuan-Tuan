package service.impl;

import config.MariaDBConnection;
import dao.PhieuXuatDAO;
import entities.ChiTietPhieuXuat;
import entities.PhieuXuat;
import entities.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import service.PhieuXuatService;

import java.rmi.RemoteException;
import java.util.List;

public class PhieuXuatServiceImpl extends GenericServiceImpl<PhieuXuat, String> implements PhieuXuatService {
    public PhieuXuatServiceImpl() throws RemoteException {
        super(new PhieuXuatDAO());
    }

    @Override
    public boolean save(PhieuXuat px) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            String chiNhanh = resolveBranch(px.getChiNhanh(), px.getNhanVien());
            px.setChiNhanh(chiNhanh);
            
            if (px.getChiTietPhieuXuats() != null) {
                for (ChiTietPhieuXuat ct : px.getChiTietPhieuXuats()) {
                    SanPham sp = em.find(SanPham.class, ct.getSanPham().getMaSP());
                    if (sp != null) {
                        int branchStock = getBranchStock(em, sp.getMaSP(), chiNhanh);
                        int newStock = branchStock - ct.getSoLuong();
                        if (newStock < 0) throw new Exception("Không đủ hàng trong kho cho SP: " + sp.getTenSP());
                    }
                    ct.setPhieuXuat(px);
                }
            }
            em.persist(px);
            
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive()) tr.rollback();
            System.err.println("Lỗi xuất kho: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    @Override
    public List<ChiTietPhieuXuat> getDetailsByProduct(String maSP) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT ct FROM ChiTietPhieuXuat ct WHERE ct.sanPham.maSP = :maSP", ChiTietPhieuXuat.class)
                    .setParameter("maSP", maSP)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public PhieuXuat findByIdWithDetails(String id) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM PhieuXuat p LEFT JOIN FETCH p.chiTietPhieuXuats WHERE p.maPhieuXuat = :id", PhieuXuat.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }

    private String resolveBranch(String chiNhanh, entities.NhanVien nv) {
        if (nv != null && nv.getTaiKhoan() != null
                && nv.getTaiKhoan().getVaiTro() == entities.enums.VaiTro.ADMIN) {
            return null; // Admin nhập/xuất ở kho chung
        }
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
