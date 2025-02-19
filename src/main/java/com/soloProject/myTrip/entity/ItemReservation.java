package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.ItemSellStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private LocalDate reservationDate;

    private int remainingSeats;

    private int totalPrice;

    private String carrierCode;

    private String carrierName;

    private int flightPrice;

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;

    @Builder
    public ItemReservation(Item item, LocalDate reservationDate, int totalPrice,
            String carrierCode, String carrierName, int flightPrice) {
        this.item = item;
        this.reservationDate = reservationDate;
        this.itemSellStatus = ItemSellStatus.SELL;
        this.remainingSeats = item.getRemainingSeats();
        this.totalPrice = totalPrice;
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.flightPrice = flightPrice;
    }

    public void updateTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
