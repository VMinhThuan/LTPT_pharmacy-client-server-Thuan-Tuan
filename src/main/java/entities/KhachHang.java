package entities;

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
@Table(name = "khachhang")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KhachHang implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "maKhachHang", unique = true)
    @EqualsAndHashCode.Include
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String maKhachHang;

    @Column(name = "hoTen", columnDefinition = "VARCHAR(50)", nullable = false)
    private String hoTen;

    @Column(name = "ngaySinh", columnDefinition = "DATE", nullable = false)
    private LocalDate ngaySinh;

    @Column(name = "gioiTinh", columnDefinition = "BOOLEAN")
    private boolean gioiTinh;

    @Column(name = "soDienThoai", columnDefinition = "VARCHAR(15)", nullable = false)
    private String soDienThoai;

    @Column(name = "ngayThamGia", columnDefinition = "DATE", nullable = false)
    private LocalDate ngayThamGia;

    @Column(name = "diemTichLuy", columnDefinition = "DOUBLE")
    private double diemTichLuy;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<HoaDon> hoaDons;
}
