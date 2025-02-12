package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.ItemSellStatus;
import com.soloProject.myTrip.constant.TravelType;
import com.soloProject.myTrip.constant.DomesticCategory;
import com.soloProject.myTrip.constant.OverseasCategory;
import com.soloProject.myTrip.constant.ThemeCategory;
import groovy.transform.ToString;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "item")
@Getter
@Setter
@ToString
public class Item {

    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemName;

    @Column(nullable = false)
    private int price;

    @Lob
    @Column(nullable = false)
    private String itemDetail;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @Column(nullable = false)
    private String destination; // 여행 목적지

    @Column(nullable = false)
    private LocalDate startDate; // 여행 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 여행 종료일

    @Column(nullable = false)
    private int minParticipants; // 최소 출발 인원

    @Column(nullable = false)
    private int maxParticipants; // 최대 참가자 수

    @Column(nullable = false)
    private int currentParticipants; // 현재 참가자 수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelType travelType;

    @Enumerated(EnumType.STRING)
    private DomesticCategory domesticCategory;

    @Enumerated(EnumType.STRING)
    private OverseasCategory overseasCategory;

    @Enumerated(EnumType.STRING)
    private ThemeCategory themeCategory;

    // 잔여 좌석 수 계산 메소드
    public int getRemainingSeats() {
        return maxParticipants - currentParticipants;
    }

    // 출발 확정 여부 확인 메소드
    public boolean isDepartureConfirmed() {
        return currentParticipants >= minParticipants;
    }
}