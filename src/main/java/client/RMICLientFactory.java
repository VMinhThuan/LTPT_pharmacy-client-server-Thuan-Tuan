package client;

import service.*;

import java.rmi.Naming;

public class RMICLientFactory {
    private static final String HOST = System.getProperty("RMI_HOST",
            System.getenv().getOrDefault("RMI_HOST", "172.20.10.7"));
    private static final int PORT = Integer.parseInt(System.getProperty("RMI_PORT",
            System.getenv().getOrDefault("RMI_PORT", "1099")));

    private static Object getService(String name) {
        try {
            return Naming.lookup("rmi://" + HOST + ":" + PORT + "/" + name);
        } catch (Exception e) {
            System.err.println("RMI lookup failed: rmi://" + HOST + ":" + PORT + "/" + name);
            e.printStackTrace();
            return null;
        }
    }

    public static EmployeeService getEmployeeService() { return (EmployeeService) getService("EmployeeService"); }
    public static AccountService getAccountService() { return (AccountService) getService("AccountService"); }
    public static SanPhamService getSanPhamService() { return (SanPhamService) getService("SanPhamService"); }
    public static KhachHangService getKhachHangService() { return (KhachHangService) getService("KhachHangService"); }
    public static HoaDonService getHoaDonService() { return (HoaDonService) getService("HoaDonService"); }
    public static DonViTinhService getDonViTinhService() { return (DonViTinhService) getService("DonViTinhService"); }
    public static NhomThuocService getNhomThuocService() { return (NhomThuocService) getService("NhomThuocService"); }
    public static ThongKeService getThongKeService() { return (ThongKeService) getService("ThongKeService"); }
    public static PhieuNhapService getPhieuNhapService() { return (PhieuNhapService) getService("PhieuNhapService"); }
    public static PhieuXuatService getPhieuXuatService() { return (PhieuXuatService) getService("PhieuXuatService"); }
}
