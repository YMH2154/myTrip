package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IamportTokenResponse {
  private IamportToken response;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IamportToken {
    private String access_token;
    private Long expired_at;
    private Long now;
  }
}