package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "hoadon")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HoaDon implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "maHoaDon", unique = true)
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String maHoaDon;

    @Column(name = "ngayLapHD", columnDefinition = "DATE", nullable = false)
    private LocalDate ngayLapHD;

    @ManyToOne
    @JoinColumn(name = "maKhachHang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "maNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "chiNhanh", columnDefinition = "VARCHAR(100)")
    private String chiNhanh;

    @Column(name = "soDiemTichLuyDuocSuDung", columnDefinition = "INT")
    private int soDiemTichLuyDuocSuDung;

    @Column(name = "tienKhachHangPhaiThanhToan", columnDefinition = "DOUBLE", nullable = false)
    private double tienKhachHangPhaiThanhToan;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ChiTietHoaDon> chiTietHoaDons;
}
