package service.impl;

import config.MariaDBConnection;
import dao.PhieuNhapDAO;
import entities.ChiTietPhieuNhap;
import entities.PhieuNhap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import service.PhieuNhapService;

import java.rmi.RemoteException;
import java.util.List;

public class PhieuNhapServiceImpl extends GenericServiceImpl<PhieuNhap, String> implements PhieuNhapService {
    public PhieuNhapServiceImpl() throws RemoteException {
        super(new PhieuNhapDAO());
    }

    @Override
    public boolean save(PhieuNhap pn) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            String chiNhanh = resolveBranch(pn.getChiNhanh(), pn.getNhanVien());
            pn.setChiNhanh(chiNhanh);
            
            // Calculate total
            double total = 0;
            if (pn.getChiTietPhieuNhaps() != null) {
                for (ChiTietPhieuNhap ct : pn.getChiTietPhieuNhaps()) {
                    total += ct.getSoLuong() * ct.getGiaNhap();
                    ct.setPhieuNhap(pn);
                }
            }
            pn.setTongTien(total);
            em.persist(pn);
            
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

    @Override
    public List<ChiTietPhieuNhap> getDetailsByProduct(String maSP) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT ct FROM ChiTietPhieuNhap ct WHERE ct.sanPham.maSP = :maSP", ChiTietPhieuNhap.class)
                    .setParameter("maSP", maSP)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<ChiTietPhieuNhap> getDetailsByProductAndBranch(String maSP, String chiNhanh) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT ct FROM ChiTietPhieuNhap ct WHERE ct.sanPham.maSP = :maSP AND ct.phieuNhap.chiNhanh = :cn", ChiTietPhieuNhap.class)
                    .setParameter("maSP", maSP)
                    .setParameter("cn", chiNhanh)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public PhieuNhap findByIdWithDetails(String id) throws RemoteException {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM PhieuNhap p LEFT JOIN FETCH p.chiTietPhieuNhaps WHERE p.maPhieuNhap = :id", PhieuNhap.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}
