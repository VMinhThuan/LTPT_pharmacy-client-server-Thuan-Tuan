package entities;

import entities.enums.ELoaiSanPham;
import entities.enums.ETinhTrangSP;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@Table(name = "sanpham")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SanPham implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "maSP", unique = true)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @EqualsAndHashCode.Include
    private String maSP;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String tenSP;

    @Column(columnDefinition = "DATE", nullable = false)
    private LocalDate ngaySX;

    @Column(columnDefinition = "DATE", nullable = false)
    private LocalDate hanSD;

    @Column(columnDefinition = "DOUBLE", nullable = false)
    private double khoiLuong;

    @Column(columnDefinition = "VARCHAR(255)")
    private String moTa;

    @Column(columnDefinition = "VARCHAR(45)", nullable = false)
    private ETinhTrangSP tinhTrangSP;

    @Column(columnDefinition = "VARCHAR(60)", nullable = false)
    private String nuocSX;

    @Column(columnDefinition = "VARCHAR(60)", nullable = false)
    private String thuongHieu;

    @Column(columnDefinition = "DOUBLE", nullable = false)
    private double giaBan;

    @Column(name = "soLuongTon", columnDefinition = "INT DEFAULT 0")
    private int soLuongTon;

    @Column(name = "nguongTonToiThieu", columnDefinition = "INT DEFAULT 10")
    private int nguongTonToiThieu;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "maDonViTinh")
    private DonViTinh donViTinh;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private ELoaiSanPham loaiSP;

    @Column(columnDefinition = "VARCHAR(255)")
    private String tacDungPhu;

    @Column(columnDefinition = "BOOLEAN")
    private boolean thuocKeDon;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "maNhomThuoc_SP_PK", referencedColumnName = "maNhomThuoc")
    private NhomThuoc nhomThuoc;

    @Column(columnDefinition = "VARCHAR(255)")
    private String loiKhuyen;

    @Column(columnDefinition = "VARCHAR(255)")
    private String congDung;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ChiTietHoaDon> chiTietHoaDons;
}
