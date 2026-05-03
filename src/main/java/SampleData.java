import entities.*;
import entities.enums.ELoaiSanPham;
import entities.enums.ETinhTrangSP;
import net.datafaker.Faker;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleData {
    public static void main(String[] args) {
        Faker faker = new Faker();
        
        // 1. Dọn dẹp và TẠO mới Database bằng JDBC thuần
        recreateDatabaseManual();

        Map<String, Object> props = new HashMap<>();
        // Chỉ dùng "update" để Hibernate tạo bảng nếu thiếu, không dùng "drop-and-create"
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("jakarta.persistence.schema-generation.database.action", "none");
        
        // Cấu hình tương thích cho MariaDB 9.6 (không hỗ trợ "returning id")
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        props.put("hibernate.jdbc.use_get_generated_keys", "true");
        props.put("hibernate.id.insert_returning_enabled", "false"); // QUAN TRỌNG: Tắt RETURNING
        props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default", props);
        EntityManager em = emf.createEntityManager();

        System.out.println("Starting large scale data seeding...");

        // --- 2. Initialize Seed Data Lists ---
        String[] tenNhomThuocVN = { "Thuốc giảm đau", "Thuốc kháng sinh", "Thuốc tiêu hóa", "Thuốc ho", "Vitamin", "Thực phẩm chức năng", "Nhỏ mắt", "Hô hấp", "Tim mạch", "Tiểu đường" };
        String[] donViTinhList = { "Viên", "Hộp", "Chai", "Ống", "Lọ", "Gói", "Gram", "Kilogram", "Milliliter", "Liter" };
        String[] hoTenVN = { "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D", "Hoàng Văn E", "Vũ Thị F", "Đặng Văn G", "Bùi Thị H", "Đỗ Văn I", "Hồ Thị K", "Lương Văn L", "Ngô Thị M", "Phan Văn N", "Quách Thị O", "Vương Văn P" };
        String[] dcVN = { "Quận 1, TP.HCM", "Gò Vấp, TP.HCM", "Cầu Giấy, Hà Nội", "Thanh Khê, Đà Nẵng", "Ninh Kiều, Cần Thơ", "Quận 3, TP.HCM", "Quận 7, TP.HCM", "Ba Đình, Hà Nội" };

        // --- 3. Seed Basic Master Data ---
        em.getTransaction().begin();
        
        List<NhomThuoc> nhomThuocs = new ArrayList<>();
        for (String ten : tenNhomThuocVN) {
            NhomThuoc nt = new NhomThuoc();
            nt.setTenNhomThuoc(ten);
            em.persist(nt);
            nhomThuocs.add(nt);
        }

        List<DonViTinh> donViTinhs = new ArrayList<>();
        for (String ten : donViTinhList) {
            DonViTinh dvt = new DonViTinh();
            dvt.setTenDonViTinh(ten);
            em.persist(dvt);
            donViTinhs.add(dvt);
        }

        // Initialize ID Generator for ChiTietHoaDon
        em.createNativeQuery("INSERT INTO id_generator (gen_name, gen_value) VALUES ('chitiethoadon_id', 0) ON DUPLICATE KEY UPDATE gen_value = 0").executeUpdate();
        
        em.getTransaction().commit();
        System.out.println("Master data (NhomThuoc, DonViTinh) seeded.");

        // --- 4. Seed Employees & Accounts ---
        em.getTransaction().begin();
        List<NhanVien> nhanViens = new ArrayList<>();
        String[] branches = {"CN Quận 1", "CN Gò Vấp", "CN Thủ Đức"};

        // Admin hệ thống
        NhanVien adminNv = new NhanVien();
        adminNv.setHoTen("Vu Minh Thuan");
        adminNv.setDiaChi("Trụ sở chính");
        adminNv.setNgayVaoLam(LocalDate.now().minusYears(4));
        adminNv.setSdt("0900000001");
        adminNv.setTinhTrangNhanVien(entities.enums.ETinhTrangNhanVien.DANG_LAM_VIEC);
        adminNv.setEmail("admin@pharmacy.vn");
        adminNv.setCccd(faker.number().digits(12));
        adminNv.setChiNhanh("Trụ sở chính");

        TaiKhoan adminTk = new TaiKhoan();
        adminTk.setMaTaiKhoan("admin");
        adminTk.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt(12)));
        adminTk.setVaiTro(entities.enums.VaiTro.ADMIN);
        adminNv.setTaiKhoan(adminTk);
        adminTk.setNhanVien(adminNv);
        em.persist(adminTk);
        em.persist(adminNv);
        nhanViens.add(adminNv);

        // Mỗi chi nhánh 1 quản lý
        List<NhanVien> managers = new ArrayList<>();
        for (int i = 0; i < branches.length; i++) {
            NhanVien manager = new NhanVien();
            manager.setHoTen(faker.options().option(hoTenVN) + " " + faker.name().lastName());
            manager.setDiaChi(faker.options().option(dcVN));
            manager.setNgayVaoLam(LocalDate.now().minusYears(faker.number().numberBetween(2, 6)));
            manager.setSdt("0" + faker.number().numberBetween(3, 9) + faker.number().digits(8));
            manager.setTinhTrangNhanVien(entities.enums.ETinhTrangNhanVien.DANG_LAM_VIEC);
            manager.setEmail("ql" + (i + 1) + "@pharmacy.vn");
            manager.setCccd(faker.number().digits(12));
            manager.setChiNhanh(branches[i]);
            manager.setQuanLy(null);

            TaiKhoan tk = new TaiKhoan();
            tk.setMaTaiKhoan("QL" + (i + 1));
            tk.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt(12)));
            tk.setVaiTro(entities.enums.VaiTro.QUANLY);

            manager.setTaiKhoan(tk);
            tk.setNhanVien(manager);
            em.persist(tk);
            em.persist(manager);
            managers.add(manager);
            nhanViens.add(manager);
        }

        // Nhân viên thuộc từng chi nhánh, gắn quản lý trực tiếp
        int staffPerBranch = 4;
        for (NhanVien manager : managers) {
            for (int i = 0; i < staffPerBranch; i++) {
                NhanVien nv = new NhanVien();
                nv.setHoTen(faker.options().option(hoTenVN) + " " + faker.name().lastName());
                nv.setDiaChi(faker.options().option(dcVN));
                nv.setNgayVaoLam(LocalDate.now().minusYears(faker.number().numberBetween(1, 5)));
                nv.setSdt("0" + faker.number().numberBetween(3, 9) + faker.number().digits(8));
                nv.setTinhTrangNhanVien(entities.enums.ETinhTrangNhanVien.DANG_LAM_VIEC);
                nv.setEmail(faker.internet().emailAddress());
                nv.setCccd(faker.number().digits(12));
                nv.setChiNhanh(manager.getChiNhanh());
                nv.setQuanLy(manager);

                TaiKhoan tk = new TaiKhoan();
                tk.setMaTaiKhoan("NV" + faker.number().numberBetween(1000, 9999));
                tk.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt(12)));
                tk.setVaiTro(entities.enums.VaiTro.NHANVIEN);

                nv.setTaiKhoan(tk);
                tk.setNhanVien(nv);
                em.persist(tk);
                em.persist(nv);
                nhanViens.add(nv);
            }
        }
        em.getTransaction().commit();
        System.out.println("Employees and Accounts seeded.");

        // --- 5. Seed Customers ---
        em.getTransaction().begin();
        List<KhachHang> khachHangs = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            KhachHang kh = new KhachHang();
            String ho = faker.options().option("Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng");
            String dem = faker.options().option("Văn", "Thị", "Minh", "Anh", "Đức", "Hữu", "Ngọc", "Tuyết", "Quang");
            String ten = faker.options().option("Hùng", "Lan", "Dũng", "Hoa", "Tuấn", "Mai", "Cường", "Trang", "Thắng", "Linh", "Thảo", "Hải", "An");
            kh.setHoTen(ho + " " + dem + " " + ten);
            kh.setNgaySinh(LocalDate.now().minusYears(faker.number().numberBetween(20, 60)));
            kh.setGioiTinh(dem.equals("Thị") || ten.matches(".*(Lan|Hoa|Mai|Trang|Linh|Thảo).*") ? false : true);
            kh.setSoDienThoai("0" + faker.number().numberBetween(3, 9) + faker.number().digits(8));
            kh.setNgayThamGia(LocalDate.now().minusMonths(faker.number().numberBetween(1, 24)));
            kh.setDiemTichLuy(0.0);
            em.persist(kh);
            khachHangs.add(kh);
        }
        em.getTransaction().commit();
        System.out.println("Customers seeded.");

        // --- 6. Seed Products (Medicines) ---
        em.getTransaction().begin();
        List<SanPham> sanPhams = new ArrayList<>();
        List<SanPham> zeroStockTestProducts = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            SanPham sp = new SanPham();
            sp.setTenSP(faker.medical().medicineName() + " " + faker.options().option("500mg", "250mg", "10ml", "Tabs"));
            sp.setNgaySX(LocalDate.now().minusMonths(faker.number().numberBetween(6, 18)));
            sp.setHanSD(LocalDate.now().plusMonths(faker.number().numberBetween(12, 36)));
            sp.setKhoiLuong(faker.number().randomDouble(2, 5, 50));
            sp.setMoTa(faker.lorem().sentence());
            sp.setTinhTrangSP(ETinhTrangSP.CON_HANG);
            sp.setNuocSX(faker.options().option("Việt Nam", "USA", "France", "Germany", "India"));
            sp.setCongDung(faker.options().option("Giảm đau", "Kháng sinh", "Bổ sung Vitamin", "Trị ho"));
            sp.setThuongHieu(faker.company().name());
            sp.setLoaiSP(faker.options().option(ELoaiSanPham.class));
            sp.setGiaBan((double) faker.number().numberBetween(20, 800) * 1000);
            sp.setSoLuongTon(0); // Bắt đầu bằng 0 để tính toán dựa trên Nhập/Xuất
            sp.setNguongTonToiThieu(faker.number().numberBetween(20, 60));
            sp.setDonViTinh(faker.options().option(donViTinhs.toArray(new DonViTinh[0])));
            if (sp.getLoaiSP() == ELoaiSanPham.THUOC) {
                sp.setTacDungPhu("Gây buồn ngủ nhẹ");
                sp.setThuocKeDon(faker.bool().bool());
                sp.setNhomThuoc(faker.options().option(nhomThuocs.toArray(new NhomThuoc[0])));
            }
            em.persist(sp);
            sanPhams.add(sp);
        }

        // Seed thêm sản phẩm test tồn = 0 cho 3 chi nhánh (không tạo lịch sử nhập/bán/xuất)
        for (String branch : branches) {
            SanPham sp = new SanPham();
            sp.setTenSP("TEST ZERO STOCK - " + branch);
            sp.setNgaySX(LocalDate.now().minusMonths(2));
            sp.setHanSD(LocalDate.now().plusMonths(18));
            sp.setKhoiLuong(10.0);
            sp.setMoTa("Sản phẩm test tồn kho 0 cho " + branch);
            sp.setTinhTrangSP(ETinhTrangSP.CON_HANG);
            sp.setNuocSX("Việt Nam");
            sp.setCongDung("Test tồn kho");
            sp.setThuongHieu("TEST-" + branch);
            sp.setLoaiSP(ELoaiSanPham.THUOC);
            sp.setGiaBan(150000.0);
            sp.setSoLuongTon(0);
            sp.setNguongTonToiThieu(30);
            sp.setDonViTinh(donViTinhs.get(0));
            sp.setNhomThuoc(nhomThuocs.get(0));
            sp.setTacDungPhu("Không");
            sp.setThuocKeDon(false);
            em.persist(sp);
            sanPhams.add(sp);
            zeroStockTestProducts.add(sp);
        }
        em.getTransaction().commit();
        System.out.println("Products initialized with 0 stock.");

        // Map to keep track of calculated stock per product
        Map<String, Integer> productStockMap = new HashMap<>();
        for (SanPham sp : sanPhams) productStockMap.put(sp.getMaSP(), 0);

        // --- 7. Seed Imports (Nhập hàng nhiều đợt) ---
        em.getTransaction().begin();
        for (SanPham sp : sanPhams) {
            if (zeroStockTestProducts.contains(sp)) {
                continue;
            }
            int imports = faker.number().numberBetween(3, 6); // Nhập 3-5 đợt
            for (int i = 0; i < imports; i++) {
                NhanVien nvNhap = faker.options().option(nhanViens.toArray(new NhanVien[0]));
                PhieuNhap pn = new PhieuNhap();
                pn.setNgayNhap(LocalDate.now().minusMonths(faker.number().numberBetween(2, 12)));
                pn.setNhaCungCap(faker.company().name());
                pn.setNhanVien(nvNhap);
                boolean isAdminImport = nvNhap != null
                        && nvNhap.getTaiKhoan() != null
                        && nvNhap.getTaiKhoan().getVaiTro() == entities.enums.VaiTro.ADMIN;
                pn.setChiNhanh(isAdminImport ? null : (nvNhap != null ? nvNhap.getChiNhanh() : null));
                
                ChiTietPhieuNhap ctpn = new ChiTietPhieuNhap();
                ctpn.setSanPham(sp);
                ctpn.setPhieuNhap(pn);
                int qty = faker.number().numberBetween(100, 300);
                ctpn.setSoLuong(qty);
                ctpn.setGiaNhap(sp.getGiaBan() * 0.7);
                ctpn.setNgayHetHan(LocalDate.now().plusMonths(faker.number().numberBetween(12, 48)));
                
                List<ChiTietPhieuNhap> list = new ArrayList<>();
                list.add(ctpn);
                pn.setChiTietPhieuNhaps(list);
                pn.setTongTien(ctpn.getSoLuong() * ctpn.getGiaNhap());
                em.persist(pn);

                // Cập nhật stock tạm thời
                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) + qty);
            }
        }
        em.getTransaction().commit();
        System.out.println("Import history (3-5 batches per product) seeded.");

        // --- 7.1 Seed điều phối kho: Admin xuất từ kho tổng sang các chi nhánh ---
        em.getTransaction().begin();
        int transferProductsPerBranch = 6;
        for (NhanVien manager : managers) {
            for (int i = 0; i < transferProductsPerBranch && i < sanPhams.size(); i++) {
                SanPham sp = sanPhams.get((i + managers.indexOf(manager) * transferProductsPerBranch) % sanPhams.size());
                if (zeroStockTestProducts.contains(sp)) {
                    continue;
                }

                int qtyNhapKhoTong = 120;   // Admin nhập vào kho tổng
                int qtyXuatSangCN = 60;     // Admin xuất điều chuyển sang chi nhánh

                // 1) Phiếu nhập kho tổng (admin)
                PhieuNhap pnTong = new PhieuNhap();
                pnTong.setNgayNhap(LocalDate.now().minusDays(faker.number().numberBetween(5, 20)));
                pnTong.setNhaCungCap("Kho tổng điều phối");
                pnTong.setNhanVien(adminNv);
                pnTong.setChiNhanh(null);

                ChiTietPhieuNhap ctNhapTong = new ChiTietPhieuNhap();
                ctNhapTong.setSanPham(sp);
                ctNhapTong.setPhieuNhap(pnTong);
                ctNhapTong.setSoLuong(qtyNhapKhoTong);
                ctNhapTong.setGiaNhap(sp.getGiaBan() * 0.65);
                ctNhapTong.setNgayHetHan(LocalDate.now().plusMonths(24));
                List<ChiTietPhieuNhap> listNhapTong = new ArrayList<>();
                listNhapTong.add(ctNhapTong);
                pnTong.setChiTietPhieuNhaps(listNhapTong);
                pnTong.setTongTien(ctNhapTong.getSoLuong() * ctNhapTong.getGiaNhap());
                em.persist(pnTong);
                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) + qtyNhapKhoTong);

                // 2) Phiếu xuất kho tổng (admin) - mục đích điều chuyển sang chi nhánh
                PhieuXuat pxTong = new PhieuXuat();
                pxTong.setNgayXuat(LocalDate.now().minusDays(faker.number().numberBetween(1, 4)));
                pxTong.setLyDoXuat("Điều chuyển sang " + manager.getChiNhanh());
                pxTong.setNhanVien(adminNv);
                pxTong.setChiNhanh(null);

                ChiTietPhieuXuat ctXuatTong = new ChiTietPhieuXuat();
                ctXuatTong.setSanPham(sp);
                ctXuatTong.setPhieuXuat(pxTong);
                ctXuatTong.setSoLuong(qtyXuatSangCN);
                List<ChiTietPhieuXuat> listXuatTong = new ArrayList<>();
                listXuatTong.add(ctXuatTong);
                pxTong.setChiTietPhieuXuats(listXuatTong);
                em.persist(pxTong);
                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) - qtyXuatSangCN);

                // 3) Phiếu nhập vào chi nhánh đích (ghi nhận hàng đã nhận)
                PhieuNhap pnChiNhanh = new PhieuNhap();
                pnChiNhanh.setNgayNhap(LocalDate.now().minusDays(faker.number().numberBetween(0, 3)));
                pnChiNhanh.setNhaCungCap("Điều chuyển từ kho tổng");
                pnChiNhanh.setNhanVien(manager);
                pnChiNhanh.setChiNhanh(manager.getChiNhanh());

                ChiTietPhieuNhap ctNhapCN = new ChiTietPhieuNhap();
                ctNhapCN.setSanPham(sp);
                ctNhapCN.setPhieuNhap(pnChiNhanh);
                ctNhapCN.setSoLuong(qtyXuatSangCN);
                ctNhapCN.setGiaNhap(sp.getGiaBan() * 0.65);
                ctNhapCN.setNgayHetHan(LocalDate.now().plusMonths(24));
                List<ChiTietPhieuNhap> listNhapCN = new ArrayList<>();
                listNhapCN.add(ctNhapCN);
                pnChiNhanh.setChiTietPhieuNhaps(listNhapCN);
                pnChiNhanh.setTongTien(ctNhapCN.getSoLuong() * ctNhapCN.getGiaNhap());
                em.persist(pnChiNhanh);
                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) + qtyXuatSangCN);
            }
        }
        em.getTransaction().commit();
        System.out.println("Admin transfer data (kho tổng -> chi nhánh) seeded.");

        // --- 7.1 Seed phân bổ: Admin xuất kho tổng sang từng chi nhánh ---
        em.getTransaction().begin();
        int transferProductCount = Math.min(12, sanPhams.size());
        int cursor = 0;
        for (NhanVien manager : managers) {
            for (int t = 0; t < 4 && cursor < transferProductCount; t++, cursor++) {
                SanPham sp = sanPhams.get(cursor);
                if (zeroStockTestProducts.contains(sp)) continue;

                int qtyTransfer = 20 + (t * 5);

                // Admin xuất khỏi kho tổng
                PhieuXuat pxAdmin = new PhieuXuat();
                pxAdmin.setNgayXuat(LocalDate.now().minusDays(7 + t));
                pxAdmin.setLyDoXuat("Phân bổ hàng cho chi nhánh " + manager.getChiNhanh());
                pxAdmin.setNhanVien(adminNv);
                pxAdmin.setChiNhanh(null);

                ChiTietPhieuXuat ctXuat = new ChiTietPhieuXuat();
                ctXuat.setPhieuXuat(pxAdmin);
                ctXuat.setSanPham(sp);
                ctXuat.setSoLuong(qtyTransfer);
                List<ChiTietPhieuXuat> listXuat = new ArrayList<>();
                listXuat.add(ctXuat);
                pxAdmin.setChiTietPhieuXuats(listXuat);
                em.persist(pxAdmin);

                // Quản lý chi nhánh nhập về kho chi nhánh của họ
                PhieuNhap pnBranch = new PhieuNhap();
                pnBranch.setNgayNhap(LocalDate.now().minusDays(6 + t));
                pnBranch.setNhaCungCap("Điều chuyển nội bộ từ kho tổng");
                pnBranch.setNhanVien(manager);
                pnBranch.setChiNhanh(manager.getChiNhanh());

                ChiTietPhieuNhap ctNhap = new ChiTietPhieuNhap();
                ctNhap.setPhieuNhap(pnBranch);
                ctNhap.setSanPham(sp);
                ctNhap.setSoLuong(qtyTransfer);
                ctNhap.setGiaNhap(sp.getGiaBan() * 0.65);
                ctNhap.setNgayHetHan(sp.getHanSD());
                List<ChiTietPhieuNhap> listNhap = new ArrayList<>();
                listNhap.add(ctNhap);
                pnBranch.setChiTietPhieuNhaps(listNhap);
                pnBranch.setTongTien(ctNhap.getSoLuong() * ctNhap.getGiaNhap());
                em.persist(pnBranch);
            }
        }
        em.getTransaction().commit();
        System.out.println("Admin-to-branch transfer history seeded.");

        // Map to track customer points
        Map<String, Double> customerPointsMap = new HashMap<>();
        for (KhachHang kh : khachHangs) customerPointsMap.put(kh.getMaKhachHang(), 0.0);

        // --- 8. Seed Sales (Hóa đơn bán lẻ - Xuất hàng) ---
        int batchSize = 20;
        for (int i = 0; i < 100; i++) {
            if (i % batchSize == 0) em.getTransaction().begin();
            
            HoaDon hd = new HoaDon();
            KhachHang kh = faker.options().option(khachHangs.toArray(new KhachHang[0]));
            NhanVien nvBan = faker.options().option(nhanViens.toArray(new NhanVien[0]));
            hd.setKhachHang(kh);
            hd.setNhanVien(nvBan);
            boolean isAdminSale = nvBan != null
                    && nvBan.getTaiKhoan() != null
                    && nvBan.getTaiKhoan().getVaiTro() == entities.enums.VaiTro.ADMIN;
            hd.setChiNhanh(isAdminSale ? null : (nvBan != null ? nvBan.getChiNhanh() : null));
            hd.setNgayLapHD(LocalDate.now().minusDays(faker.number().numberBetween(0, 30)));
            
            List<ChiTietHoaDon> cthds = new ArrayList<>();
            int itemTypes = faker.number().numberBetween(1, 4);
            double total = 0;
            for (int j = 0; j < itemTypes; j++) {
                SanPham sp = faker.options().option(sanPhams.toArray(new SanPham[0]));
                int sellQty = faker.number().numberBetween(1, 5);
                
                ChiTietHoaDon cthd = new ChiTietHoaDon();
                cthd.setHoaDon(hd);
                cthd.setSanPham(sp);
                cthd.setSoLuong(sellQty);
                cthd.setVAT(0.08);
                total += (sp.getGiaBan() * cthd.getSoLuong()) * 1.08;
                cthds.add(cthd);

                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) - sellQty);
            }
            hd.setTienKhachHangPhaiThanhToan(total);
            hd.setSoDiemTichLuyDuocSuDung(0);
            hd.setChiTietHoaDons(cthds);
            em.persist(hd);
            
            // Tích lũy điểm vào Map
            customerPointsMap.put(kh.getMaKhachHang(), customerPointsMap.get(kh.getMaKhachHang()) + (total * 0.05));
            
            if ((i + 1) % batchSize == 0 || i == 99) {
                em.getTransaction().commit();
            }
        }
        System.out.println("Sales history seeded.");

        // --- 9. Seed Manual Exports (Xuất hàng khác) ---
        em.getTransaction().begin();
        for (SanPham sp : sanPhams) {
            if (zeroStockTestProducts.contains(sp)) {
                continue;
            }
            if (faker.bool().bool()) {
                NhanVien nvXuat = faker.options().option(nhanViens.toArray(new NhanVien[0]));
                PhieuXuat px = new PhieuXuat();
                px.setNgayXuat(LocalDate.now().minusDays(faker.number().numberBetween(1, 10)));
                px.setLyDoXuat(faker.options().option("Hàng hỏng", "Hết hạn", "Trả hàng NCC"));
                px.setNhanVien(nvXuat);
                boolean isAdminExport = nvXuat != null
                        && nvXuat.getTaiKhoan() != null
                        && nvXuat.getTaiKhoan().getVaiTro() == entities.enums.VaiTro.ADMIN;
                px.setChiNhanh(isAdminExport ? null : (nvXuat != null ? nvXuat.getChiNhanh() : null));
                
                ChiTietPhieuXuat ctpx = new ChiTietPhieuXuat();
                ctpx.setSanPham(sp);
                ctpx.setPhieuXuat(px);
                int exportQty = faker.number().numberBetween(5, 15);
                ctpx.setSoLuong(exportQty);
                
                List<ChiTietPhieuXuat> list = new ArrayList<>();
                list.add(ctpx);
                px.setChiTietPhieuXuats(list);
                em.persist(px);

                // Trừ stock tạm thời
                productStockMap.put(sp.getMaSP(), productStockMap.get(sp.getMaSP()) - exportQty);
            }
        }
        em.getTransaction().commit();
        System.out.println("Manual exports seeded.");

        // --- 10. Update Final Stock Quantities to Products ---
        em.getTransaction().begin();
        for (SanPham sp : sanPhams) {
            SanPham managedSp = em.find(SanPham.class, sp.getMaSP());
            managedSp.setSoLuongTon(productStockMap.get(sp.getMaSP()));
            em.merge(managedSp);
        }

        // Seed có chủ đích để test cảnh báo:
        // - 4 sản phẩm hạn < 3 tháng
        // - 4 sản phẩm hạn từ 3-6 tháng
        // - tất cả đều bị tồn kho thấp (soLuongTon < nguongTonToiThieu)
        int forcedAlerts = Math.min(8, sanPhams.size());
        for (int i = 0; i < forcedAlerts; i++) {
            SanPham source = sanPhams.get(i);
            SanPham managedSp = em.find(SanPham.class, source.getMaSP());
            int minStock = 40 + (i * 5);
            managedSp.setNguongTonToiThieu(minStock);
            managedSp.setSoLuongTon(Math.max(0, minStock - (15 + i)));
            if (i < 4) {
                managedSp.setHanSD(LocalDate.now().plusDays(25 + (i * 10L))); // < 3 tháng
            } else {
                managedSp.setHanSD(LocalDate.now().plusDays(100 + ((i - 4) * 15L))); // khoảng 3-6 tháng
            }
            em.merge(managedSp);
        }

        // Seed thêm sản phẩm tồn kho = 0 để test ghi chú "Số lượng tồn thấp"
        int zeroStockSeed = Math.min(5, sanPhams.size() - forcedAlerts);
        for (int i = 0; i < zeroStockSeed; i++) {
            SanPham source = sanPhams.get(forcedAlerts + i);
            SanPham managedSp = em.find(SanPham.class, source.getMaSP());
            managedSp.setNguongTonToiThieu(25 + (i * 5));
            managedSp.setSoLuongTon(0);
            em.merge(managedSp);
        }
        em.getTransaction().commit();
        System.out.println("Final stock quantities synchronized.");

        // --- 11. Update Final Points to Customers ---
        em.getTransaction().begin();
        for (KhachHang kh : khachHangs) {
            KhachHang managedKh = em.find(KhachHang.class, kh.getMaKhachHang());
            managedKh.setDiemTichLuy(customerPointsMap.get(kh.getMaKhachHang()));
            em.merge(managedKh);
        }
        em.getTransaction().commit();
        System.out.println("Final customer points synchronized.");

        em.close();
        emf.close();
        System.out.println("\nSUCCESS: All data seeded successfully!");
        System.out.println("- Products: 50");
        System.out.println("- Employees: 15");
        System.out.println("- Customers: 40");
        System.out.println("- Invoices: 100");
    }

    private static void recreateDatabaseManual() {
        System.out.println("Recreating database manually for MariaDB 9.6 compatibility...");
        String[] dropTables = {
            "chitiethpeuxuat", "chitiethpeunhap", "chitiethoadon", 
            "phieuxuat", "phieunhap", "hoadon", "nhanvien", 
            "sanpham", "taikhoan", "khachhang", "nhomthuoc", "donvitinh", "id_generator"
        };
        
        String[] createTables = {
            "create table donvitinh (maDonViTinh varchar(255) not null, tenDonViTinh VARCHAR(50) not null, primary key (maDonViTinh)) engine=InnoDB",
            "create table nhomthuoc (maNhomThuoc varchar(255) not null, tenNhomThuoc VARCHAR(100) not null, primary key (maNhomThuoc)) engine=InnoDB",
            "create table taikhoan (maTaiKhoan VARCHAR(50) not null, password VARCHAR(100) not null, vaiTro enum ('ADMIN','NHANVIEN','QUANLY'), primary key (maTaiKhoan)) engine=InnoDB",
            "create table khachhang (diemTichLuy DOUBLE, gioiTinh BOOLEAN, ngaySinh DATE not null, ngayThamGia DATE not null, hoTen VARCHAR(50) not null, maKhachHang varchar(255) not null, soDienThoai VARCHAR(15) not null, primary key (maKhachHang)) engine=InnoDB",
            "create table sanpham (giaBan DOUBLE not null, hanSD DATE not null, khoiLuong DOUBLE not null, loaiSP VARCHAR(50) not null, ngaySX DATE not null, soLuongTon INT DEFAULT 0, nguongTonToiThieu INT DEFAULT 10, thuocKeDon BOOLEAN, tinhTrangSP VARCHAR(45) not null, congDung VARCHAR(255), loiKhuyen VARCHAR(255), maDonViTinh varchar(255), maNhomThuoc_SP_PK varchar(255), maSP varchar(255) not null, moTa VARCHAR(255), nuocSX VARCHAR(60) not null, tacDungPhu VARCHAR(255), tenSP VARCHAR(255) not null, thuongHieu VARCHAR(60) not null, primary key (maSP), foreign key (maDonViTinh) references donvitinh (maDonViTinh), foreign key (maNhomThuoc_SP_PK) references nhomthuoc (maNhomThuoc)) engine=InnoDB",
            "create table nhanvien (ngayVaoLam DATE not null, tinhTrangNhanVien tinyint, cccd VARCHAR(50) not null, chiNhanh VARCHAR(100), diaChi VARCHAR(50) not null, email VARCHAR(50) not null, hoTen VARCHAR(50) not null, maNhanVien VARCHAR(50) not null, maQuanLy VARCHAR(50), maTaiKhoan VARCHAR(50), sdt VARCHAR(15) not null, primary key (maNhanVien), foreign key (maTaiKhoan) references taikhoan (maTaiKhoan), foreign key (maQuanLy) references nhanvien (maNhanVien)) engine=InnoDB",
            "create table hoadon (ngayLapHD DATE not null, soDiemTichLuyDuocSuDung INT, tienKhachHangPhaiThanhToan DOUBLE not null, chiNhanh VARCHAR(100), maHoaDon varchar(255) not null, maKhachHang varchar(255) not null, maNhanVien VARCHAR(50) not null, primary key (maHoaDon), foreign key (maKhachHang) references khachhang (maKhachHang), foreign key (maNhanVien) references nhanvien (maNhanVien)) engine=InnoDB",
            "create table chitiethoadon (VAT DOUBLE not null, soLuong INT not null, id bigint not null, maHoaDon varchar(255) not null, maSanPham varchar(255) not null, primary key (id), foreign key (maHoaDon) references hoadon (maHoaDon), foreign key (maSanPham) references sanpham (maSP)) engine=InnoDB",
            "create table phieunhap (ngayNhap DATE not null, tongTien DOUBLE, chiNhanh VARCHAR(100), maNhanVien VARCHAR(50) not null, maPhieuNhap varchar(255) not null, nhaCungCap VARCHAR(255), primary key (maPhieuNhap), foreign key (maNhanVien) references nhanvien (maNhanVien)) engine=InnoDB",
            "create table chitiethpeunhap (giaNhap float(53) not null, ngayHetHan date, soLuong integer not null, id bigint not null auto_increment, maPhieuNhap varchar(255) not null, maSP varchar(255) not null, primary key (id), foreign key (maPhieuNhap) references phieunhap (maPhieuNhap), foreign key (maSP) references sanpham (maSP)) engine=InnoDB",
            "create table phieuxuat (ngayXuat DATE not null, lyDoXuat VARCHAR(255), chiNhanh VARCHAR(100), maNhanVien VARCHAR(50) not null, maPhieuXuat varchar(255) not null, primary key (maPhieuXuat), foreign key (maNhanVien) references nhanvien (maNhanVien)) engine=InnoDB",
            "create table chitiethpeuxuat (soLuong integer not null, id bigint not null auto_increment, maPhieuXuat varchar(255) not null, maSP varchar(255) not null, primary key (id), foreign key (maPhieuXuat) references phieuxuat (maPhieuXuat), foreign key (maSP) references sanpham (maSP)) engine=InnoDB",
            "create table id_generator (gen_value bigint, gen_name varchar(255) not null, primary key (gen_name)) engine=InnoDB"
        };
        
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mariadb://localhost:3306/pharmacy", "root", "123456");
             java.sql.Statement stmt = conn.createStatement()) {
            
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : dropTables) {
                try { stmt.execute("DROP TABLE IF EXISTS " + table); } catch (Exception e) {}
            }
            for (String sql : createTables) {
                stmt.execute(sql);
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("Database cleared and recreated successfully.");
            
        } catch (Exception e) {
            System.err.println("Manual recreation failed: " + e.getMessage());
        }
    }
}
