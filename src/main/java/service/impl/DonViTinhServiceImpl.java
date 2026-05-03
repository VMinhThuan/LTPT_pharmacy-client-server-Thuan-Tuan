package service.impl;

import dao.DonViTinhDAO;
import entities.DonViTinh;
import service.DonViTinhService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class DonViTinhServiceImpl extends GenericServiceImpl<DonViTinh, String> implements DonViTinhService {
    private final DonViTinhDAO donViTinhDAO;

    public DonViTinhServiceImpl() throws RemoteException {
        super(new DonViTinhDAO());
        this.donViTinhDAO = (DonViTinhDAO) genericDAO;
    }

    @Override
    public boolean addProductUnit(DonViTinh donViTinh) throws Exception {
        return donViTinhDAO.save(donViTinh);
    }

    @Override
    public boolean removeProductUnit(String maDonViTinh) throws Exception {
        return donViTinhDAO.delete(maDonViTinh);
    }

    @Override
    public boolean updateProductUnit(DonViTinh donViTinh) throws Exception {
        return donViTinhDAO.update(donViTinh);
    }

    @Override
    public List<DonViTinh> getAllProductUnit() throws Exception {
        List<DonViTinh> raw = donViTinhDAO.getAll();
        List<DonViTinh> safe = new ArrayList<>(raw.size());
        for (DonViTinh dvt : raw) {
            DonViTinh copy = new DonViTinh();
            copy.setMaDonViTinh(dvt.getMaDonViTinh());
            copy.setTenDonViTinh(dvt.getTenDonViTinh());
            safe.add(copy);
        }
        return safe;
    }

    @Override
    public List<DonViTinh> getAll() throws Exception {
        return getAllProductUnit();
    }

    @Override
    public boolean save(DonViTinh donViTinh) throws Exception {
        return addProductUnit(donViTinh);
    }

    @Override
    public boolean update(DonViTinh donViTinh) throws Exception {
        return updateProductUnit(donViTinh);
    }
}
