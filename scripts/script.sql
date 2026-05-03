select * from sanpham;
select * from khachhang;
select * from hoadon;
select * from chitiethoadon;
select * from nhanvien;
select * from taikhoan;
select nhanvien.hoTen, nhanvien.email, nhanvien.sdt, taikhoan.password from nhanvien join taikhoan on nhanvien.maNhanVien = taikhoan.maNhanVien;
update nhanvien set hoTen = 'Vu Minh Thuan' where maTaiKhoan = 'TK0';


ALTER TABLE taikhoan DROP FOREIGN KEY IF EXISTS FK9lupyi9pufop3bkwjm4ig97u;

-- Thêm cột vaiTro kiểu ENUM
ALTER TABLE taikhoan ADD COLUMN vaiTro ENUM('ADMIN', 'NHANVIEN', 'QUANLY');


-- Xóa bảng vaitro cũ
DROP TABLE IF EXISTS vaitro;


SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_NAME IS NOT NULL
  AND TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'taikhoan';

ALTER TABLE nhanvien ADD COLUMN maTaiKhoan VARCHAR(50);


ALTER TABLE nhanvien
    ADD CONSTRAINT FK_NhanVien_TaiKhoan
        FOREIGN KEY (maTaiKhoan)
            REFERENCES taikhoan(maTaiKhoan);