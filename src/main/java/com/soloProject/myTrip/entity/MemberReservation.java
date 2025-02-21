package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class MemberReservation {
    @Id
    @Column(name = "member_reservation_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_reservation_id")
    private ItemReservation itemReservation;

    @Column(unique = true, nullable = false)
    private String reservationNumber;

    @Column(nullable = false)
    private String totalDeposit;

    @OneToMany(mappedBy = "memberReservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
}
