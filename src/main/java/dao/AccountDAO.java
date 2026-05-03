package dao;

import config.MariaDBConnection;
import entities.TaiKhoan;
import jakarta.persistence.EntityManager;

public class AccountDAO extends GenericDAO<TaiKhoan, String>{

    public AccountDAO() {
        super(TaiKhoan.class);
    }

    public TaiKhoan findByUsername(String maTaiKhoan){
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t FROM TaiKhoan t WHERE t.maTaiKhoan = :maTaiKhoan",
                            TaiKhoan.class)
                    .setParameter("maTaiKhoan", maTaiKhoan)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        } finally {
            em.close();
        }
    }
}
