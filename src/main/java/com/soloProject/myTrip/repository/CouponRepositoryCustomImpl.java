package com.soloProject.myTrip.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soloProject.myTrip.constant.CouponDuration;
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
            coupon.isAlphaCoupon.isTrue(),
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
            coupon.isAlphaCoupon.isTrue(),
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

    switch (searchDateType) {
      case "1w":
        return QCoupon.coupon.couponDuration.eq(CouponDuration.WEEK);
      case "1m":
        return QCoupon.coupon.couponDuration.eq(CouponDuration.MONTH);
      case "3m":
        return QCoupon.coupon.couponDuration.eq(CouponDuration.THREE);
      case "6m":
        return QCoupon.coupon.couponDuration.eq(CouponDuration.SIX);
      case "1y":
        return QCoupon.coupon.couponDuration.eq(CouponDuration.YEAR);
      default:
        return null;
    }
  }

  private BooleanExpression couponTypeEq(String couponType) {
    if (StringUtils.isEmpty(couponType)) {
      return null;
    }
    return QCoupon.coupon.couponType.eq(CouponType.valueOf(couponType));
  }

  private BooleanExpression minAmountLoe(Integer minAmount) {
    if (minAmount == null || minAmount == 0) {
      return null;
    }
    return QCoupon.coupon.minPurchaseAmount.goe(minAmount);
  }

  private BooleanExpression searchByLike(String searchQuery) {
    if (StringUtils.isEmpty(searchQuery)) {
      return null;
    }
    return QCoupon.coupon.description.like("%" + searchQuery + "%");
  }
}