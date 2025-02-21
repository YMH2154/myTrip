package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PaymentData extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_data_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ReservationStatus reservationStatus;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

}
