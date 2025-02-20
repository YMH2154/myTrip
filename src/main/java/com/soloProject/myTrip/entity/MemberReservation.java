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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_reservation_id")
    private ItemReservation itemReservation;

    @OneToMany(mappedBy = "memberReservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;
}
