package entities;

import entities.enums.VaiTro;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Data
@Table(name = "taikhoan")
public class TaiKhoan implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "maTaiKhoan", columnDefinition = "VARCHAR(50)", unique = true, nullable = false)
    private String maTaiKhoan;
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String password;

    @OneToOne(mappedBy = "taiKhoan")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NhanVien nhanVien;

    @Enumerated(EnumType.STRING)
    @Column(name = "vaiTro")
    private VaiTro vaiTro;
}
