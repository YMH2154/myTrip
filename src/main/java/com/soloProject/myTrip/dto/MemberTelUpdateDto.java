package com.soloProject.myTrip.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class MemberTelUpdateDto {
  @NotEmpty(message = "전화번호를 입력해주세요")
  @Length(max = 13, message = "13자 이하의 전화번호를 입력해주세요")
  private String tel;
}
