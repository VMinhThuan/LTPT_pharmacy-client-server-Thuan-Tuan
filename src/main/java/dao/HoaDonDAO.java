package dao;

import config.MariaDBConnection;
import entities.HoaDon;
import jakarta.persistence.EntityManager;

public class HoaDonDAO extends GenericDAO<HoaDon, String> {
    public HoaDonDAO() {
        super(HoaDon.class);
    }

    public HoaDon findByIdWithDetails(String id) {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            HoaDon hd = em.createQuery(
                "SELECT h FROM HoaDon h LEFT JOIN FETCH h.chiTietHoaDons c LEFT JOIN FETCH c.sanPham WHERE h.maHoaDon = :id", 
                HoaDon.class)
                .setParameter("id", id)
                .getSingleResult();
            return hd;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }
    public java.util.List<entities.ChiTietHoaDon> getDetailsByProduct(String maSP) {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery(
                "SELECT c FROM ChiTietHoaDon c WHERE c.sanPham.maSP = :maSP", 
                entities.ChiTietHoaDon.class)
                .setParameter("maSP", maSP)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
