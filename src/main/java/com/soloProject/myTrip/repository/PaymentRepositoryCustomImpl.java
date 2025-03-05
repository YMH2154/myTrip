package com.soloProject.myTrip.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
        .where(searchByLike(paymentSearchDto.getSearchBy(), paymentSearchDto.getSearchQuery()))
        .orderBy(payment.regTime.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = queryFactory
        .selectFrom(payment)
        .join(payment.memberReservation, memberReservation)
        .join(memberReservation.member, member)
        .where(searchByLike(paymentSearchDto.getSearchBy(), paymentSearchDto.getSearchQuery()))
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
}