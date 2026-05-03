# Pharmacy Backend

Hệ thống quản lý hiệu thuốc (Pharmacy Management System) - Backend.

## 📋 Prerequisites (Yêu cầu hệ thống)

- **Java Development Kit (JDK) 17** hoặc mới hơn.
- **MariaDB >= 10.5** (khuyến nghị 10.11 LTS) đang chạy trên cổng 3306.
- **Gradle** (đã bao gồm `gradlew` trong project).

## 🔄 Nâng cấp MariaDB để tương thích Hibernate

Project hiện dùng Hibernate 7 + MariaDB JDBC 3.5.x. Nếu DB cũ (ví dụ 9.6), có thể lỗi cú pháp như `SQLSyntaxErrorException` khi Hibernate chạy DDL (`ALTER TABLE IF EXISTS ...`).

### 1. Kiểm tra version hiện tại

```sql
SELECT VERSION();
SHOW VARIABLES LIKE 'version%';
```

### 2. Backup database trước khi upgrade

Linux/macOS:
```bash
mysqldump -u root -p --single-transaction --routines --triggers pharmacy > pharmacy_backup_$(date +%F_%H%M).sql
```

Windows (PowerShell):
```powershell
mysqldump -u root -p --single-transaction --routines --triggers pharmacy > pharmacy_backup.sql
```

Backup toàn bộ server (khuyến nghị cho production):
```bash
mysqldump -u root -p --all-databases --single-transaction --routines --triggers --events > full_backup.sql
```

### 3. Upgrade MariaDB

#### Ubuntu/Debian (APT)
```bash
sudo systemctl stop mariadb
sudo apt update
sudo apt install mariadb-server
sudo systemctl start mariadb
sudo mariadb-upgrade -u root -p
```

Nếu repository mặc định chưa có >=10.5, thêm repo MariaDB phù hợp distro rồi chạy lại:
```bash
sudo apt update
sudo apt install mariadb-server mariadb-client
```

#### RHEL/CentOS/Rocky/AlmaLinux (DNF/YUM)
```bash
sudo systemctl stop mariadb
sudo dnf install MariaDB-server MariaDB-client
sudo systemctl start mariadb
sudo mariadb-upgrade -u root -p
```

#### Windows Server/Windows local
1. Backup bằng `mysqldump` như trên.
2. Tải MariaDB Installer (10.5+), chạy nâng cấp "in-place".
3. Verify service `MariaDB` đã chạy lại.
4. Chạy upgrade metadata:
```powershell
"C:\Program Files\MariaDB 10.11\bin\mariadb-upgrade.exe" -u root -p
```

### 4. Kiểm tra lại sau nâng cấp

```sql
SELECT VERSION();
CHECK TABLE san_pham;
```

Chạy app:
```bash
./gradlew clean build
```
Sau đó chạy `Runner` để Hibernate khởi tạo EntityManager và thực hiện `hbm2ddl.auto=update`.

### 5. Rollback nếu có sự cố

1. Dừng ứng dụng và MariaDB mới.
2. Cài lại đúng version MariaDB cũ (đã dùng trước upgrade).
3. Khởi tạo data dir rỗng.
4. Restore backup:
```bash
mysql -u root -p < full_backup.sql
```
hoặc
```bash
mysql -u root -p pharmacy < pharmacy_backup_YYYY-MM-DD_HHMM.sql
```
5. Start lại app để xác nhận kết nối.

### 6. Rủi ro chính khi upgrade

1. DDL mới có thể thay đổi collation/charset mặc định.
2. SQL mode thay đổi có thể làm query cũ fail (đặc biệt `ONLY_FULL_GROUP_BY`).
3. Plugin auth khác version có thể làm user cũ đăng nhập fail.
4. Dung lượng ổ đĩa không đủ trong lúc backup/restore.

Giảm rủi ro:
1. Backup full + test restore trên môi trường staging.
2. Ghi lại config cũ trong `my.cnf`/`my.ini` (charset, sql_mode, innodb settings).
3. Chạy smoke test CRUD sau khi upgrade trước khi mở traffic thật.

## 🚀 Setup & Running (Cài đặt và Chạy)

### 1. Chuẩn bị Cơ sở dữ liệu
Mở MariaDB Client (HeidiSQL, MySQL Workbench, hoặc CLI) và chạy lệnh sau:
```sql
CREATE DATABASE IF NOT EXISTS pharmacy;
```

### 2. Cấu hình kết nối
Kiểm tra tệp `src/main/resources/META-INF/persistence.xml` và cập nhật thông tin đăng nhập nếu cần:
```xml
<property name="jakarta.persistence.jdbc.user" value="root" />
<property name="jakarta.persistence.jdbc.password" value="123456" />
```

### 3. Build Project
Sử dụng terminal tại thư mục gốc của project:
```bash
./gradlew build
```

### 4. Chạy Seeding Dữ liệu mẫu (Tùy chọn)
Để có dữ liệu ban đầu để test, hãy chạy class `SampleData`:
1. Mở IDE (IntelliJ IDEA, VS Code, Eclipse).
2. Tìm đến tệp `src/main/java/SampleData.java`.
3. Nhấn nút **Run** (phím tắt thường là Shift+F10 hoặc Ctrl+R).

### 5. Chạy RMI Server (Dành cho ứng dụng phân tán)
Nếu bạn muốn chạy Backend như một Server cung cấp dịch vụ cho Client:
1. Mở class `src/main/java/server/RMIServer.java`.
2. Nhấn nút **Run**.
Server sẽ chạy trên cổng mặc định `1099`.

### 6. Chạy Kiểm tra Connection
Chạy class `Runner.java` để khởi tạo database và kiểm tra kết nối:
1. Mở class `src/main/java/Runner.java`.
2. Nhấn nút **Run**.

## 📂 Cấu trúc Thư mục
- `entities`: Các lớp ánh xạ database (JPA Entities).
- `dao`: Lớp truy xuất dữ liệu (Data Access Objects). Đã hỗ trợ đầy đủ cho tất cả các Entity chính.
- `service`: Lớp xử lý nghiệp vụ (Business Logic). Đã triển khai RMI-ready services.
- `config`: Cấu hình hệ thống (MariaDB Connection).
- `scripts`: Các tệp SQL hỗ trợ.

## 🛠 Hướng dẫn Phát triển tiếp
1. **Thêm Entity mới**: Tạo class trong `entities`, thêm `@Entity` và đăng ký class đó trong `persistence.xml`.
2. **Sử dụng DAO**: Tất cả các entity như `SanPham`, `KhachHang`, `HoaDon`, `NhanVien`, `TaiKhoan` đều đã có DAO tương ứng.
3. **Sử dụng Service**: Sử dụng các Service như `SanPhamService`, `EmployeeService`, v.v. để thao tác với dữ liệu thay vì gọi trực tiếp DAO.
4. **RMI Support**: Các service hiện tại đã kế thừa `UnicastRemoteObject`, sẵn sàng cho việc triển khai ứng dụng phân tán (Distributed System).

---
*Phát triển bởi [Tên của bạn/Team]*
