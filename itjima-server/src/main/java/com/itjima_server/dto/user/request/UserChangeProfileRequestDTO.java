package com.itjima_server.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "전화번호 변경 요청 DTO")
public class UserChangeProfileRequestDTO {

    @Schema(description = "전화번호 (숫자 10~11자리)", example = "01012345678")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자 10~11자리여야 합니다.")
    private String phone;

    @Schema(description = "현재 비밀번호 (영문, 숫자, 특수문자 포함 8~64자)", example = "Test1234!")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,64}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String currentPassword;

    @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함 8~64자)", example = "Test1234!")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,64}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;

}
