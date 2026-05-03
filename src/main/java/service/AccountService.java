package service;

import entities.TaiKhoan;

public interface AccountService extends GenericService<TaiKhoan, String> {
    TaiKhoan logIn(String maTaiKhoan, String password) throws Exception;
    boolean changePassword(String maTaiKhoan, String oldPassword, String newPassword) throws Exception;
    boolean resetPassword(String maTaiKhoan) throws Exception;
}
