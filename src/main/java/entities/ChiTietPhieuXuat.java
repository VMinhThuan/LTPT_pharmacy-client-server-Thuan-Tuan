package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Entity
@Table(name = "chitiethpeuxuat")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietPhieuXuat implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maPhieuXuat", nullable = false)
    private PhieuXuat phieuXuat;

    @ManyToOne
    @JoinColumn(name = "maSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "soLuong", nullable = false)
    private int soLuong;
}
