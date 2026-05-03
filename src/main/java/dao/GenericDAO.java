package dao;

import config.MariaDBConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.ArrayList;

public abstract class GenericDAO<T, O> {
    protected Class<T> clazz;

    public GenericDAO(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T find(O o){
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.find(clazz, o);
        } finally {
            em.close();
        }
    }

    public List<T> getAll() {
        EntityManager em = MariaDBConnection.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + clazz.getSimpleName() + " e", clazz).getResultList();
        } catch (Exception e) {
            System.err.println("Error in getAll for " + clazz.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public boolean save(T t){
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
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

    public boolean update(T t){
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
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

    public boolean delete(O o){
        EntityManager em = MariaDBConnection.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            T t = em.find(clazz, o);
            if (t != null){
                em.remove(t);
                tr.commit();
                return true;
            }
            return false;
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
