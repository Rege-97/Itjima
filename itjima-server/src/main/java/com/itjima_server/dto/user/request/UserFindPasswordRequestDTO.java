package com.itjima_server.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 찾기 요청 DTO")
public class UserFindPasswordRequestDTO {

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 32, message = "이름은 2~32자여야 합니다.")
    private String name;

    @Schema(description = "전화번호 (숫자 10~11자리)", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자 10~11자리여야 합니다.")
    private String phone;

    @Schema(description = "이메일", example = "honggildong@example.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
