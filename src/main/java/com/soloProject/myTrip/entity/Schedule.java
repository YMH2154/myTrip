package com.soloProject.myTrip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "activities", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "activity")
    private List<String> activity; // 관광지 명

    @ElementCollection
    @CollectionTable(name = "activity_image_urls", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "image_url")
    private List<String> imageUrl; // 관광지 이미지(url)

    @ElementCollection
    @CollectionTable(name = "activity_descriptions", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "description")
    private List<String> description; // 관광지 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}
