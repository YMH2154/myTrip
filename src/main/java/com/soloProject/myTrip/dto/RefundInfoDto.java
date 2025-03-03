package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.entity.MemberReservation;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RefundInfoDto {
    private String reservationNumber;
    private String itemName;
    private List<String> thumbnailImageUrls;
    private String memberName;
    private String memberEmail;
    private String memberTel;
    private ReservationStatus reservationStatus;
    private Integer totalPrice;
    private Integer totalDeposit;
    private List<PaymentDto> payments;
    private String  departureDateTime;
    private String  returnDateTime;

    public static RefundInfoDto createDto(MemberReservation reservation, List<PaymentDto> paymentDtos){
        RefundInfoDto refundInfo = new RefundInfoDto();
        refundInfo.setReservationNumber(reservation.getReservationNumber());
        refundInfo.setItemName(reservation.getItemReservation().getItem().getItemName());
        refundInfo.setThumbnailImageUrls(reservation.getItemReservation().getItem().getThumbnailImageUrls());
        refundInfo.setMemberName(reservation.getMember().getName());
        refundInfo.setMemberEmail(reservation.getMember().getEmail());
        refundInfo.setMemberTel(reservation.getMember().getTel());
        refundInfo.setReservationStatus(reservation.getReservationStatus());
        refundInfo.setTotalPrice(reservation.getTotalPrice());
        refundInfo.setTotalDeposit(reservation.getTotalDeposit());
        refundInfo.setDepartureDateTime(reservation.getItemReservation().getDepartureDateTime());
        refundInfo.setReturnDateTime(reservation.getItemReservation().getReturnDateTime());
        refundInfo.setPayments(paymentDtos);
        return refundInfo;
    }
} 