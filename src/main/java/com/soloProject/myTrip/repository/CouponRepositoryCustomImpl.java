package com.soloProject.myTrip.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soloProject.myTrip.constant.CouponType;
import com.soloProject.myTrip.dto.CouponSearchDto;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.QCoupon;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CouponRepositoryCustomImpl implements CouponRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public CouponRepositoryCustomImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public Page<Coupon> getAdminCouponPage(CouponSearchDto searchDto, Pageable pageable) {
    QCoupon coupon = QCoupon.coupon;

    List<Coupon> content = queryFactory
        .selectFrom(coupon)
        .where(
            searchDateTypeEq(searchDto.getSearchDateType()),
            couponTypeEq(searchDto.getCouponType()),
            minAmountLoe(searchDto.getMinAmount()),
            searchByLike(searchDto.getSearchQuery()))
        .orderBy(coupon.id.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = queryFactory
        .select(coupon.count())
        .from(coupon)
        .where(
            searchDateTypeEq(searchDto.getSearchDateType()),
            couponTypeEq(searchDto.getCouponType()),
            minAmountLoe(searchDto.getMinAmount()),
            searchByLike(searchDto.getSearchQuery()))
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression searchDateTypeEq(String searchDateType) {
    if (StringUtils.isEmpty(searchDateType) || "all".equals(searchDateType)) {
      return null;
    }

    LocalDateTime dateTime = LocalDateTime.now();

    switch (searchDateType) {

      case "1w":
        dateTime = dateTime.minusWeeks(1);
        break;
      case "1m":
        dateTime = dateTime.minusMonths(1);
        break;
      case "3m":
        dateTime = dateTime.minusMonths(3);
        break;
      case "6m":
        dateTime = dateTime.minusMonths(6);
        break;
      case "1y":
        dateTime = dateTime.minusYears(1);
        break;
    }

    return QCoupon.coupon.startDate.goe(dateTime.toLocalDate());
  }

  private BooleanExpression couponTypeEq(String couponType) {
    if (StringUtils.isEmpty(couponType)) {
      return null;
    }
    return QCoupon.coupon.couponType.eq(CouponType.valueOf(couponType));
  }

  private BooleanExpression minAmountLoe(Long minAmount) {
    if (minAmount == null || minAmount == 0) {
      return null;
    }
    return QCoupon.coupon.minPurchaseAmount.loe(new java.math.BigDecimal(minAmount));
  }

  private BooleanExpression searchByLike(String searchQuery) {
    if (StringUtils.isEmpty(searchQuery)) {
      return null;
    }
    return QCoupon.coupon.description.like("%" + searchQuery + "%");
  }
}