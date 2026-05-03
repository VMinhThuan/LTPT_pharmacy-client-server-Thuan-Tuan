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
@Table(name = "phieunhap")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PhieuNhap implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maPhieuNhap", unique = true)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @EqualsAndHashCode.Include
    private String maPhieuNhap;

    @Column(name = "ngayNhap", columnDefinition = "DATE", nullable = false)
    private LocalDate ngayNhap;

    @ManyToOne
    @JoinColumn(name = "maNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "nhaCungCap", columnDefinition = "VARCHAR(255)")
    private String nhaCungCap;

    @Column(name = "chiNhanh", columnDefinition = "VARCHAR(100)")
    private String chiNhanh;

    @Column(name = "tongTien", columnDefinition = "DOUBLE")
    private double tongTien;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ChiTietPhieuNhap> chiTietPhieuNhaps;
}
