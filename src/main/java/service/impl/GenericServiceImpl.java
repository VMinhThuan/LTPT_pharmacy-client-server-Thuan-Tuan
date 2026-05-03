package service.impl;

import dao.GenericDAO;
import service.GenericService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public abstract class GenericServiceImpl<T, O> extends UnicastRemoteObject implements GenericService<T, O> {
    protected GenericDAO<T, O> genericDAO;

    public GenericServiceImpl(GenericDAO<T, O> genericDAO) throws RemoteException {
        this.genericDAO = genericDAO;
    }

    @Override
    public List<T> getAll() throws Exception {
        return genericDAO.getAll();
    }

    @Override
    public boolean save(T t) throws Exception {
        return genericDAO.save(t);
    }

    @Override
    public boolean update(T t) throws Exception {
        return genericDAO.update(t);
    }

    @Override
    public T find(O o) throws Exception {
        return genericDAO.find(o);
    }

    @Override
    public boolean delete(O o) throws Exception {
        return genericDAO.delete(o);
    }
}
