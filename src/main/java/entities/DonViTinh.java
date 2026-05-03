package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "donvitinh")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DonViTinh implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "maDonViTinh", unique = true)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @EqualsAndHashCode.Include
    private String maDonViTinh;
    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String tenDonViTinh;

    @OneToMany(mappedBy = "donViTinh")
    @ToString.Exclude
    private List<SanPham> sanPhamList;
}
