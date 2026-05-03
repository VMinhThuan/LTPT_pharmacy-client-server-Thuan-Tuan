package service.impl;

import dao.EmployeeDAO;
import entities.NhanVien;
import service.EmployeeService;

import java.rmi.RemoteException;

public class EmployeeServiceImpl extends GenericServiceImpl<NhanVien, String> implements EmployeeService {
    public EmployeeServiceImpl() throws RemoteException {
        super(new EmployeeDAO());
    }
}
