package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IamportResponse {
  private int code;
  private String message;
  private IamportPayment response;
}