package entities;

import entities.HoaDon;
import entities.SanPham;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Data
@Entity
@Table(name = "chitiethoadon")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietHoaDon implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "cthd_id_gen")
    @TableGenerator(
            name = "cthd_id_gen",
            table = "id_generator",
            pkColumnName = "gen_name",
            valueColumnName = "gen_value",
            pkColumnValue = "chitiethoadon_id",
            allocationSize = 1
    )
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maHoaDon", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "maSanPham", nullable = false)
    private SanPham sanPham;

    @Column(name = "soLuong", columnDefinition = "INT", nullable = false)
    private int soLuong;

    @Column(name = "VAT", columnDefinition = "DOUBLE", nullable = false)
    private double VAT;
}
