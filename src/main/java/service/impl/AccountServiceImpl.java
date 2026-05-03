package service.impl;

import dao.AccountDAO;
import entities.TaiKhoan;
import org.mindrot.jbcrypt.BCrypt;
import service.AccountService;

import java.rmi.RemoteException;

public class AccountServiceImpl extends GenericServiceImpl<TaiKhoan, String> implements AccountService {
    public AccountServiceImpl() throws RemoteException {
        super(new AccountDAO());
    }

    @Override
    public TaiKhoan logIn(String maTaiKhoan, String password) throws Exception {
        TaiKhoan account = ((AccountDAO) genericDAO).findByUsername(maTaiKhoan);
        if (account == null || password == null) return null;

        String stored = account.getPassword();
        if (stored == null || stored.isBlank()) return null;

        // Backward compatibility for legacy plaintext records.
        if (!isBcryptHash(stored)) {
            if (!stored.equals(password)) return null;
            account.setPassword(hash(password));
            genericDAO.update(account);
            return account;
        }

        return BCrypt.checkpw(password, stored) ? account : null;
    }

    @Override
    public boolean save(TaiKhoan taiKhoan) throws Exception {
        if (taiKhoan == null || taiKhoan.getPassword() == null || taiKhoan.getPassword().isBlank()) {
            return false;
        }
        if (!isBcryptHash(taiKhoan.getPassword())) {
            taiKhoan.setPassword(hash(taiKhoan.getPassword()));
        }
        return super.save(taiKhoan);
    }

    @Override
    public boolean update(TaiKhoan taiKhoan) throws Exception {
        if (taiKhoan == null || taiKhoan.getMaTaiKhoan() == null || taiKhoan.getMaTaiKhoan().isBlank()) {
            return false;
        }
        TaiKhoan existing = genericDAO.find(taiKhoan.getMaTaiKhoan());
        if (existing == null) return false;

        String incoming = taiKhoan.getPassword();
        if (incoming == null || incoming.isBlank()) {
            taiKhoan.setPassword(existing.getPassword());
        } else if (!isBcryptHash(incoming)) {
            taiKhoan.setPassword(hash(incoming));
        }
        return super.update(taiKhoan);
    }

    @Override
    public boolean changePassword(String maTaiKhoan, String oldPassword, String newPassword) throws Exception {
        TaiKhoan account = logIn(maTaiKhoan, oldPassword);
        if (account == null) return false;
        
        account.setPassword(hash(newPassword));
        return genericDAO.update(account);
    }

    @Override
    public boolean resetPassword(String maTaiKhoan) throws Exception {
        TaiKhoan account = genericDAO.find(maTaiKhoan);
        if (account == null) return false;
        
        account.setPassword(hash("12345678"));
        return genericDAO.update(account);
    }

    private String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }

    private boolean isBcryptHash(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}
