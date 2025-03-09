package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_merchant_uid", columnNames = { "merchant_uid" })
})
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    @Column(name = "merchant_uid", nullable = false)
    private String merchantUid;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    private String paymentKey;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_reservation_id")
    private MemberReservation memberReservation;

    private String cancelReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_coupon_id")
    private Coupon usedCoupon;

    private Integer usedMileage;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
}
