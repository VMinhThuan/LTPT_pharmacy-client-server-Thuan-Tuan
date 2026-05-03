package dao;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class ThongKeDAO {

    public ThongKeDAO() {
    }

    public List<Object[]> getDoanhThuTheoNgay(LocalDate start, LocalDate end) {
        EntityManager em = config.MariaDBConnection.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT h.ngayLapHD, SUM(h.tienKhachHangPhaiThanhToan) " +
                    "FROM HoaDon h " +
                    "WHERE h.ngayLapHD BETWEEN :start AND :end " +
                    "GROUP BY h.ngayLapHD " +
                    "ORDER BY h.ngayLapHD", Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getTopSanPhamBanChay(LocalDate start, LocalDate end, int limit) {
        EntityManager em = config.MariaDBConnection.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT c.sanPham.tenSP, SUM(c.soLuong) as totalQty " +
                    "FROM ChiTietHoaDon c " +
                    "WHERE c.hoaDon.ngayLapHD BETWEEN :start AND :end " +
                    "GROUP BY c.sanPham.tenSP " +
                    "ORDER BY totalQty DESC", Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getHieuSuatNhanVien(LocalDate start, LocalDate end) {
        EntityManager em = config.MariaDBConnection.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT h.nhanVien.hoTen, COUNT(h), SUM(h.tienKhachHangPhaiThanhToan) " +
                    "FROM HoaDon h " +
                    "WHERE h.ngayLapHD BETWEEN :start AND :end " +
                    "GROUP BY h.nhanVien.hoTen " +
                    "ORDER BY SUM(h.tienKhachHangPhaiThanhToan) DESC", Object[].class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
