package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Age {
    INFANT("유아"),
    CHILD("아동"),
    ADULT("성인");

    private final String description;
}
