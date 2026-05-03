package service.impl;

import dao.NhomThuocDAO;
import entities.NhomThuoc;
import service.NhomThuocService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NhomThuocServiceImpl extends GenericServiceImpl<NhomThuoc, String> implements NhomThuocService {
    private final NhomThuocDAO nhomThuocDAO;

    public NhomThuocServiceImpl() throws RemoteException {
        super(new NhomThuocDAO());
        this.nhomThuocDAO = (NhomThuocDAO) genericDAO;
    }

    @Override
    public boolean addDrugGroup(NhomThuoc nhomThuoc) throws Exception {
        return nhomThuocDAO.save(nhomThuoc);
    }

    @Override
    public boolean removeDrugGroup(String maNhomThuoc) throws Exception {
        return nhomThuocDAO.delete(maNhomThuoc);
    }

    @Override
    public List<NhomThuoc> getAllDrugGroup() throws Exception {
        List<NhomThuoc> raw = nhomThuocDAO.getAll();
        List<NhomThuoc> safe = new ArrayList<>(raw.size());
        for (NhomThuoc nt : raw) {
            NhomThuoc copy = new NhomThuoc();
            copy.setMaNhomThuoc(nt.getMaNhomThuoc());
            copy.setTenNhomThuoc(nt.getTenNhomThuoc());
            safe.add(copy);
        }
        return safe;
    }

    @Override
    public List<NhomThuoc> getAll() throws Exception {
        return getAllDrugGroup();
    }

    @Override
    public boolean save(NhomThuoc nhomThuoc) throws Exception {
        return addDrugGroup(nhomThuoc);
    }
} 
