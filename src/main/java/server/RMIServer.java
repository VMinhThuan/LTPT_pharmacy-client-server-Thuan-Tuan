package server;

import service.*;
import service.impl.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    private static final int DEFAULT_PORT = 1099;

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("RMI_PORT", String.valueOf(DEFAULT_PORT)));
            String host = System.getProperty("RMI_HOST",
                    System.getenv().getOrDefault("RMI_HOST", "172.20.10.7"));
            System.setProperty("java.rmi.server.hostname", host);

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.println("Created new RMI registry on port " + port);
            } catch (RemoteException ex) {
                registry = LocateRegistry.getRegistry("127.0.0.1", port);
                registry.list(); // verify reachable registry
                System.out.println("RMI registry already exists on port " + port + ", reusing it.");
            }

            // Bind services
            registry.rebind("EmployeeService", new EmployeeServiceImpl());
            registry.rebind("AccountService", new AccountServiceImpl());
            registry.rebind("SanPhamService", new SanPhamServiceImpl());
            registry.rebind("KhachHangService", new KhachHangServiceImpl());
            registry.rebind("HoaDonService", new HoaDonServiceImpl());
            registry.rebind("ChiTietHoaDonService", new ChiTietHoaDonServiceImpl());
            registry.rebind("DonViTinhService", new DonViTinhServiceImpl());
            registry.rebind("NhomThuocService", new NhomThuocServiceImpl());
            registry.rebind("ThongKeService", new ThongKeServiceImpl());
            registry.rebind("PhieuNhapService", new PhieuNhapServiceImpl());
            registry.rebind("PhieuXuatService", new PhieuXuatServiceImpl());

            System.out.println("Pharmacy RMI Server is running at rmi://" + host + ":" + port + "/...");
        } catch (Exception e) {
            System.err.println("RMI Server exception:");
            e.printStackTrace();
        }
    }
}
