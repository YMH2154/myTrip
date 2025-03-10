package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.ItemSellStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int remainingSeats;

    private int totalPrice;

    private Boolean departureConfirmed;

    @OneToMany(mappedBy = "itemReservation", cascade = CascadeType.ALL)
    private List<MemberReservation> memberReservations;

    // 출국편 정보
    private String departureDateTime; // 시간 포함 출발 일시
    private String departureCarrierCode;
    private String departureCarrierName;
    private String departureFlightNumber;

    // 귀국편 정보
    private String returnDateTime; // 시간 포함 귀국 일시
    private String returnCarrierCode;
    private String returnCarrierName;
    private String returnFlightNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ItemSellStatus itemSellStatus;

    @Builder
    public ItemReservation(Item item, String departureDateTime, String returnDateTime,
            String departureCarrierCode, String departureCarrierName, String departureFlightNumber,
            String returnCarrierCode, String returnCarrierName, String returnFlightNumber,
            int totalPrice) {
        this.item = item;
        this.departureDateTime = departureDateTime;
        this.returnDateTime = returnDateTime;
        this.itemSellStatus = ItemSellStatus.SELL;
        this.departureConfirmed = false;
        this.remainingSeats = item.getMaxParticipants();
        this.totalPrice = totalPrice;

        // 출국편 정보
        this.departureCarrierCode = departureCarrierCode;
        this.departureCarrierName = departureCarrierName;
        this.departureFlightNumber = departureFlightNumber;

        // 귀국편 정보
        this.returnCarrierCode = returnCarrierCode;
        this.returnCarrierName = returnCarrierName;
        this.returnFlightNumber = returnFlightNumber;
    }

    public void updateRemainingSeats(int updatedSeats) {
        this.remainingSeats = updatedSeats;
        if (this.remainingSeats == 0) {
            this.itemSellStatus = ItemSellStatus.WAITING;
        } else {
            this.itemSellStatus = ItemSellStatus.SELL;
        }
    }

    public void soldOutReservation() {
        this.itemSellStatus = ItemSellStatus.SOLDOUT;
    }
}
