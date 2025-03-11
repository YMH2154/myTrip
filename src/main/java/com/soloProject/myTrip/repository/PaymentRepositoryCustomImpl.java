package com.soloProject.myTrip.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.dto.PaymentSearchDto;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.entity.QPayment;
import com.soloProject.myTrip.entity.QMemberReservation;
import com.soloProject.myTrip.entity.QMember;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.util.List;

public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public PaymentRepositoryCustomImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public Page<Payment> getAdminPaymentPage(PaymentSearchDto paymentSearchDto, Pageable pageable) {
    QPayment payment = QPayment.payment;
    QMemberReservation memberReservation = QMemberReservation.memberReservation;
    QMember member = QMember.member;

    List<Payment> content = queryFactory
        .selectFrom(payment)
        .join(payment.memberReservation, memberReservation).fetchJoin()
        .join(memberReservation.member, member).fetchJoin()
        .where(
            searchByLike(paymentSearchDto.getSearchBy(), paymentSearchDto.getSearchQuery()),
            paymentMethodEq(paymentSearchDto.getPaymentMethod()),
            reservationStatusEq(paymentSearchDto.getReservationStatus()),
            paymentTypeEq(paymentSearchDto.getPaymentType())
        )
        .orderBy(payment.regTime.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = queryFactory
        .selectFrom(payment)
        .join(payment.memberReservation, memberReservation)
        .join(memberReservation.member, member)
        .where(
            searchByLike(paymentSearchDto.getSearchBy(), paymentSearchDto.getSearchQuery()),
            paymentMethodEq(paymentSearchDto.getPaymentMethod()),
            reservationStatusEq(paymentSearchDto.getReservationStatus()),
            paymentTypeEq(paymentSearchDto.getPaymentType())
        )
        .fetchCount();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression searchByLike(String searchBy, String searchQuery) {
    if (StringUtils.isEmpty(searchQuery) || StringUtils.isEmpty(searchBy)) {
      return null;
    }

    QPayment payment = QPayment.payment;
    QMemberReservation memberReservation = QMemberReservation.memberReservation;
    QMember member = QMember.member;

    if (searchBy.equals("email")) {
      return member.email.like("%" + searchQuery + "%");
    } else if (searchBy.equals("itemName")) {
      return payment.itemName.like("%" + searchQuery + "%");
    } else if (searchBy.equals("merchantUid")) {
      return payment.merchantUid.like("%" + searchQuery + "%");
    }

    return null;
  }

  private BooleanExpression paymentMethodEq(String paymentMethod) {
    if (StringUtils.isEmpty(paymentMethod)) {
      return null;
    }
    return QPayment.payment.paymentMethod.eq(PaymentMethod.valueOf(paymentMethod));
  }

  private BooleanExpression reservationStatusEq(String reservationStatus) {
    if (StringUtils.isEmpty(reservationStatus)) {
      return null;
    }
    return QPayment.payment.memberReservation.reservationStatus.eq(ReservationStatus.valueOf(reservationStatus));
  }

  private BooleanExpression paymentTypeEq(String paymentType) {
    if (StringUtils.isEmpty(paymentType)) {
      return null;
    }
    return QPayment.payment.paymentType.eq(PaymentType.valueOf(paymentType));
  }
}