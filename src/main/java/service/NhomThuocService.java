package service;

import entities.NhomThuoc;

import java.rmi.RemoteException;
import java.util.List;

public interface NhomThuocService extends GenericService<NhomThuoc, String> {
    boolean addDrugGroup(NhomThuoc nhomThuoc) throws Exception;
    boolean removeDrugGroup(String maNhomThuoc) throws Exception;
    List<NhomThuoc> getAllDrugGroup() throws Exception;
} 