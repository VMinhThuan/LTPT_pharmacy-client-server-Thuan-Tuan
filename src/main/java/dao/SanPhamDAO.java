package dao;

import config.MariaDBConnection;
import entities.DonViTinh;
import entities.NhomThuoc;
import entities.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class SanPhamDAO extends GenericDAO<SanPham, String> {

    public SanPhamDAO() {
        super(SanPham.class);
    }

    @Override
    public boolean update(SanPham input) {
        if (input == null || input.getMaSP() == null || input.getMaSP().isBlank()) {
            return false;
        }

        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            SanPham managed = em.find(SanPham.class, input.getMaSP());
            if (managed == null) {
                tr.rollback();
                return false;
            }

            managed.setTenSP(input.getTenSP());
            managed.setNgaySX(input.getNgaySX());
            managed.setHanSD(input.getHanSD());
            managed.setKhoiLuong(input.getKhoiLuong());
            managed.setMoTa(input.getMoTa());
            managed.setTinhTrangSP(input.getTinhTrangSP());
            managed.setNuocSX(input.getNuocSX());
            managed.setThuongHieu(input.getThuongHieu());
            managed.setGiaBan(input.getGiaBan());
            managed.setSoLuongTon(input.getSoLuongTon());
            managed.setNguongTonToiThieu(input.getNguongTonToiThieu());
            managed.setLoaiSP(input.getLoaiSP());
            managed.setTacDungPhu(input.getTacDungPhu());
            managed.setThuocKeDon(input.isThuocKeDon());
            managed.setLoiKhuyen(input.getLoiKhuyen());
            managed.setCongDung(input.getCongDung());

            if (input.getDonViTinh() != null && input.getDonViTinh().getMaDonViTinh() != null) {
                DonViTinh dvt = em.find(DonViTinh.class, input.getDonViTinh().getMaDonViTinh());
                managed.setDonViTinh(dvt);
            } else {
                managed.setDonViTinh(null);
            }

            if (input.getNhomThuoc() != null && input.getNhomThuoc().getMaNhomThuoc() != null) {
                NhomThuoc nt = em.find(NhomThuoc.class, input.getNhomThuoc().getMaNhomThuoc());
                managed.setNhomThuoc(nt);
            } else {
                managed.setNhomThuoc(null);
            }

            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }
}
