package dao;

import entities.NhanVien;

public class EmployeeDAO extends GenericDAO<NhanVien, String> {

    public EmployeeDAO() {
        super(NhanVien.class);
    }
}
