package service;

import java.rmi.Remote;
import java.util.List;

public interface GenericService<T, O> extends Remote {
    List<T> getAll() throws Exception;
    boolean save(T t) throws Exception;
    boolean update(T t) throws Exception;
    T find(O o) throws Exception;
    boolean delete(O o) throws Exception;
}
