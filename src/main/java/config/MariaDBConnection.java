package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class MariaDBConnection {
    private static final String PERSISTENCE_UNIT_NAME = "default";
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            ensurePasswordColumnLength();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf.isOpen() && emf != null){
            emf.close();
        }
    }

    /**
     * Backward-compatible schema patch:
     * old databases had taikhoan.password as VARCHAR(50),
     * while BCrypt hash needs at least 60 chars.
     */
    private static void ensurePasswordColumnLength() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.createNativeQuery("ALTER TABLE taikhoan MODIFY password VARCHAR(100) NOT NULL").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception ignored) {
            // Ignore if table doesn't exist yet or column already compatible.
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) em.close();
        }
    }
}
