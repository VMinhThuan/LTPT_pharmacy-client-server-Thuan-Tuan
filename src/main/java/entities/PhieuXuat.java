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
@Table(name = "phieuxuat")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PhieuXuat implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maPhieuXuat", unique = true)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @EqualsAndHashCode.Include
    private String maPhieuXuat;

    @Column(name = "ngayXuat", columnDefinition = "DATE", nullable = false)
    private LocalDate ngayXuat;

    @ManyToOne
    @JoinColumn(name = "maNhanVien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "lyDoXuat", columnDefinition = "VARCHAR(255)")
    private String lyDoXuat;

    @Column(name = "chiNhanh", columnDefinition = "VARCHAR(100)")
    private String chiNhanh;

    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ChiTietPhieuXuat> chiTietPhieuXuats;
}
