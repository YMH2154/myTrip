package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.constant.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
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

   @Column(name = "merchant_uid", unique = true, nullable = false)
   private String merchantUid; // 주문 번호

   @Column(name = "amount", nullable = false)
   private Integer amount; // 결제된 금액

   private String paymentKey;

   @Enumerated(EnumType.STRING)
   private PaymentMethod paymentMethod;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "member_reservation_id")
   private MemberReservation memberReservation; // 예약

   private String cancelReason;

   @OneToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "used_coupon_id")
   private Coupon usedCoupon;

   private Integer usedMileage;

   @Enumerated(EnumType.STRING)
   private PaymentType paymentType;
}
