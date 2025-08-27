package com.itjima_server.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 재설정 요청 DTO")
public class UserPasswordResetRequestDTO {

    @Schema(description = "인증코드", example = "A1B2C3")
    @NotBlank(message = "인증코드는 필수입니다.")
    @Size(min = 6, max = 6, message = "인증코드는 6자리 입니다.")
    private String code;

    @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8~64자)", example = "Test1234!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,64}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
}
