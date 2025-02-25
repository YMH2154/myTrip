package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
public class MemberReservation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_reservation_id")
    private ItemReservation itemReservation;

    @Column(nullable = false, unique = true)
    private String reservationNumber;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @OneToMany(mappedBy = "memberReservation", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @OneToOne(mappedBy = "memberReservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    private int totalPrice;
    private int totalDeposit;

    public void updateStatus(ReservationStatus status) {
        this.setReservationStatus(status);
    }
}
