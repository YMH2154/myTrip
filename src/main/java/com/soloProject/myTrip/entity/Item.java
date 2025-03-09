package com.soloProject.myTrip.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.dto.ItemFormDto;
import groovy.transform.ToString;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity {

    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelType travelType;

    @Enumerated(EnumType.STRING)
    private DomesticCategory domesticCategory;

    @Enumerated(EnumType.STRING)
    private OverseasCategory overseasCategory;

    @Enumerated(EnumType.STRING)
    private ThemeCategory themeCategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private AirportCode origin; // 출발 공항

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private AirportCode destination; // 도착 공항

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int lowestPrice;

    @Column(nullable = false)
    private String itemDetailImageUrl;

    @Column(nullable = false)
    private int maxParticipants; // 최대 참가자 수

    @Column(nullable = false)
    private int minParticipants; // 최소 출발 인원

    @ElementCollection
    @CollectionTable(name = "thumbnail_image_urls", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "thumbnail_image_url")
    private List<String> thumbnailImageUrls;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemReservation> reservations;

    @Column(nullable = false)
    private int night;
    @Column(nullable = false)
    private int duration;

    private boolean hasLeader; // 인솔자 유무
    private boolean hasGuideFee; // 가이드 경비 유무
    private int guideFee; // 가이드 경비 금액

    @Enumerated(EnumType.STRING)
    private CurrencyUnit guideFeeUnit; // 가이드 경비 통화 단위

    private boolean hasShopping; // 쇼핑 유무
    private int shoppingCount; // 쇼핑 횟수
    private boolean hasInsurance; // 여행자 보험 유무

    @Column(columnDefinition = "integer default 0")
    private Integer reservationCount;

    @Transient
    private LocalDate earliestDepartureDate;

    @Transient
    private String airline;

    public void plusReservationCount() {
        this.reservationCount++;
    }

    public void minusReservationCount(){this.reservationCount--;}

    public void updateItem(ItemFormDto itemFormDto) {
        this.setItemName(itemFormDto.getItemName());
        this.setTravelType(itemFormDto.getTravelType());
        this.setDomesticCategory(itemFormDto.getDomesticCategory());
        this.setOverseasCategory(itemFormDto.getOverseasCategory());
        this.setThemeCategory(itemFormDto.getThemeCategory());
        this.setDestination(itemFormDto.getDestination());
        this.setPrice(itemFormDto.getPrice());
        this.setItemDetailImageUrl(itemFormDto.getItemDetailImageUrl());
        this.setMaxParticipants(itemFormDto.getMaxParticipants());
        this.setMinParticipants(itemFormDto.getMinParticipants());
        this.setDuration(itemFormDto.getDuration());
        this.hasLeader = itemFormDto.isHasLeader();
        this.hasGuideFee = itemFormDto.isHasGuideFee();
        this.guideFee = itemFormDto.isHasGuideFee()
                ? (itemFormDto.getGuideFee() != null ? itemFormDto.getGuideFee() : 0)
                : 0;
        this.guideFeeUnit = itemFormDto.getGuideFeeUnit();
        this.hasShopping = itemFormDto.isHasShopping();
        this.shoppingCount = itemFormDto.isHasShopping()
                ? (itemFormDto.getShoppingCount() != null ? itemFormDto.getShoppingCount() : 0)
                : 0;
        this.hasInsurance = itemFormDto.isHasInsurance();
    }
}