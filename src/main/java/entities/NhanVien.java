package entities;

import entities.enums.ETinhTrangNhanVien;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "nhanvien")
public class NhanVien implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(columnDefinition = "VARCHAR(50)", unique = true, nullable = false)
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String maNhanVien;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String hoTen;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String diaChi;

    @Temporal(TemporalType.DATE)
    @Column(columnDefinition = "DATE", nullable = false)
    private LocalDate ngayVaoLam;

    @Column(columnDefinition = "VARCHAR(15)", nullable = false)
    private String sdt;

    private ETinhTrangNhanVien tinhTrangNhanVien;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String email;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String cccd;

    @Column(columnDefinition = "VARCHAR(100)")
    private String chiNhanh;

    @ManyToOne
    @JoinColumn(name = "maQuanLy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NhanVien quanLy;

    @OneToMany(mappedBy = "quanLy")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<NhanVien> nhanViensDuocQuanLy;

    @OneToOne
    @JoinColumn(name = "maTaiKhoan", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaiKhoan taiKhoan;

    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<HoaDon> hoaDons;
}
