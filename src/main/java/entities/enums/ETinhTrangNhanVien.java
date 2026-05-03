package entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ETinhTrangNhanVien {
    DANG_LAM_VIEC("đang làm việc"), NGHI_PHEP("nghỉ phép"), NGHI_VIEC("nghỉ việc");
    private final String tinhTrang;
}
