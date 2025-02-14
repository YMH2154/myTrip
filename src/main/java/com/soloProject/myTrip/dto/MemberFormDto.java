package com.soloProject.myTrip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class MemberFormDto {
    private Long id;

    @NotEmpty(message = "이름을 입력해주세요.")
    private String name;

    @NotEmpty(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일을 입력해주세요")
    private String email;

    @NotEmpty(message = "비밀번호를 입력해주세요")
//    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()-+=])[A-Za-z\\d!@#$%^&*()-+=]{8,16}$",
//            message = "비밀번호는 8자 이상, 16자 이하로 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotEmpty(message = "인증 코드를 입력하세요")
    private String inputCode;

    @NotEmpty(message = "비밀번호 확인을 입력해주세요")
    private String checkPassword;

    @NotEmpty(message = "전화번호를 입력해주세요")
    @Length(max = 12,message = "12자 이하의 전화번호를 입력해주세요")
    private String tel;

}
