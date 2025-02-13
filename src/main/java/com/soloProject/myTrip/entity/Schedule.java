package com.soloProject.myTrip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @Column(nullable = false)
    private int day; // 몇 일차 일정인지 ex)1일차 2일차

    @Column(nullable = false)
    private String activity; // 관광지 명

    @Column(nullable = false)
    private String imageUrl; // 관광지 이미지(url)

    @Column(nullable = false, length = 500)
    private String description; // 관광지 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}
