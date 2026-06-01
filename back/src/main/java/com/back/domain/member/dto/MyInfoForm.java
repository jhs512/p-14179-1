package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyInfoForm {

    @NotBlank
    @Size(min = 2, max = 30)
    private String nickname;

    // 비워두면 비밀번호 변경 안 함 (입력 시 컨트롤러에서 길이 검증)
    private String password;
}
