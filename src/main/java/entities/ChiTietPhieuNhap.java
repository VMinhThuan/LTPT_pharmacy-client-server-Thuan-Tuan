package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "chitiethpeunhap")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietPhieuNhap implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maPhieuNhap", nullable = false)
    private PhieuNhap phieuNhap;

    @ManyToOne
    @JoinColumn(name = "maSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "soLuong", nullable = false)
    private int soLuong;

    @Column(name = "giaNhap", nullable = false)
    private double giaNhap;

    @Column(name = "ngayHetHan")
    private LocalDate ngayHetHan;
}
