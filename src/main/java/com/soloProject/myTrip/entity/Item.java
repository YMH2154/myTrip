package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.dto.ItemFormDto;
import groovy.transform.ToString;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

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
    private int duration;

    @Column(nullable = false)
    private int price;

    private int totalPrice;

    @Lob
    @Column(nullable = false)
    private String itemDetail;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @Column(nullable = false)
    private int remainingSeats; // 최대 참가자 수

    @Column(nullable = false)
    private int minParticipants; // 최소 출발 인원

    @Column(nullable = false)
    private int currentParticipants; // 현재 참가자 수

    @ElementCollection
    @CollectionTable(name = "item_image_urls", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "item_image_url")
    private List<String> itemImageUrls;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;

    @Column(nullable = false)
    private LocalDate departureDate; // 출발일

    @Column(nullable = false)
    private LocalDate returnDate; // 도착일

    // 잔여 좌석 수 계산 메소드
    public int getRemainingSeats() {
        return remainingSeats - currentParticipants;
    }

    // 출발 확정 여부 확인 메소드
    public boolean isDepartureConfirmed() {
        return currentParticipants >= minParticipants;
    }

    public void updateItem(ItemFormDto itemFormDto) {
        this.setItemName(itemFormDto.getItemName());
        this.setTravelType(itemFormDto.getTravelType());
        this.setDomesticCategory(itemFormDto.getDomesticCategory());
        this.setOverseasCategory(itemFormDto.getOverseasCategory());
        this.setThemeCategory(itemFormDto.getThemeCategory());
        this.setDestination(itemFormDto.getDestination());
        this.setDuration(itemFormDto.getDuration());
        this.setPrice(itemFormDto.getPrice());
        this.setItemDetail(itemFormDto.getItemDetail());
        this.setRemainingSeats(itemFormDto.getRemainingSeats());
        this.setMinParticipants(itemFormDto.getMinParticipants());
        this.setDepartureDate(itemFormDto.getDepartureDate());
        this.setReturnDate(itemFormDto.getReturnDate());
    }
}