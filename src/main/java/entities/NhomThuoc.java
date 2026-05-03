package entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "nhomthuoc")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NhomThuoc implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "maNhomThuoc", unique = true)
    @EqualsAndHashCode.Include
    private String maNhomThuoc;
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String tenNhomThuoc;

    @OneToMany(mappedBy = "nhomThuoc")
    private List<SanPham> sanPhamList;
}
