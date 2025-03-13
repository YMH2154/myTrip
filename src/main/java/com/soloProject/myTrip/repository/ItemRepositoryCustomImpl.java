package com.soloProject.myTrip.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soloProject.myTrip.constant.DomesticCategory;
import com.soloProject.myTrip.constant.OverseasCategory;
import com.soloProject.myTrip.constant.ThemeCategory;
import com.soloProject.myTrip.constant.TravelType;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.QItem;
import com.soloProject.myTrip.service.MainService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QItem item = QItem.item; // QItem 인스턴스를 클래스 레벨에서 선언

    @PersistenceContext
    private EntityManager entityManager;

    public ItemRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    private BooleanExpression searchTravelTypeEq(TravelType travelType) {
        return travelType == null
                ? null
                : QItem.item.travelType.eq(travelType);
    }

    private BooleanExpression searchOverseasCategory(OverseasCategory overseasCategory) {
        return overseasCategory == null
                ? null
                : QItem.item.overseasCategory.eq(overseasCategory);
    }

    private BooleanExpression searchDomesticCategory(DomesticCategory domesticCategory) {
        return domesticCategory == null
                ? null
                : QItem.item.domesticCategory.eq(domesticCategory);
    }

    private BooleanExpression searchThemeCategory(ThemeCategory themeCategory) {
        return themeCategory == null
                ? null
                : QItem.item.themeCategory.eq(themeCategory);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1W", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }
        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (StringUtils.equals("itemName", searchBy)) {
            return QItem.item.itemName.like("%" + searchQuery + "%");
        } else if (StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        } else {
            return null;
        }
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        List<Item> content = queryFactory
                .selectFrom(item) // 클래스 레벨에서 선언한 QItem 사용
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchTravelTypeEq(itemSearchDto.getTravelType()),
                        searchOverseasCategory(itemSearchDto.getOverseasCategory()),
                        searchDomesticCategory(itemSearchDto.getDomesticCategory()),
                        searchThemeCategory(itemSearchDto.getThemeCategory()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchTravelTypeEq(itemSearchDto.getTravelType()),
                        searchOverseasCategory(itemSearchDto.getOverseasCategory()),
                        searchDomesticCategory(itemSearchDto.getDomesticCategory()),
                        searchThemeCategory(itemSearchDto.getThemeCategory()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Item> findItemsByCategory(String request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 상위 카테고리별 검색 조건 추가
        switch (request.toLowerCase()) {
            case "domestic": {
                builder.and(item.travelType.eq(TravelType.DOMESTIC));
                break;
            }
            case "asia", "아시아": {
                List<OverseasCategory> asiaCategories = Arrays.stream(OverseasCategory.values())
                        .filter(category -> "아시아".equals(category.getRegion()))
                        .collect(Collectors.toList());
                builder.and(item.travelType.eq(TravelType.OVERSEAS))
                        .and(item.overseasCategory.in(asiaCategories));
                break;
            }
            case "europe", "유럽": {
                List<OverseasCategory> europeCategories = Arrays.stream(OverseasCategory.values())
                        .filter(category -> "유럽".equals(category.getRegion()))
                        .collect(Collectors.toList());
                builder.and(item.travelType.eq(TravelType.OVERSEAS))
                        .and(item.overseasCategory.in(europeCategories));
                break;
            }
            case "america", "미주": {
                List<OverseasCategory> americaCategories = Arrays.stream(OverseasCategory.values())
                        .filter(category -> "미주".equals(category.getRegion()))
                        .collect(Collectors.toList());
                builder.and(item.travelType.eq(TravelType.OVERSEAS))
                        .and(item.overseasCategory.in(americaCategories));
                break;
            }
            case "theme": {
                builder.and(item.travelType.eq(TravelType.THEME));
                break;
            }
            default: {
                // 세부 카테고리 검색
                boolean categoryFound = false;

                // 국내여행 카테고리 검색
                for (DomesticCategory category : DomesticCategory.values()) {
                    if (category.getDescription().equals(request)) {
                        builder.and(item.travelType.eq(TravelType.DOMESTIC))
                                .and(item.domesticCategory.eq(category));
                        categoryFound = true;
                        break;
                    }
                }

                // 해외여행 카테고리 검색
                if (!categoryFound) {
                    for (OverseasCategory category : OverseasCategory.values()) {
                        if (category.getDescription().equals(request)) {
                            builder.and(item.travelType.eq(TravelType.OVERSEAS))
                                    .and(item.overseasCategory.eq(category));
                            categoryFound = true;
                            break;
                        }
                    }
                }

                // 테마여행 카테고리 검색
                if (!categoryFound) {
                    for (ThemeCategory category : ThemeCategory.values()) {
                        if (category.getDescription().equals(request)) {
                            builder.and(item.travelType.eq(TravelType.THEME))
                                    .and(item.themeCategory.eq(category));
                            break;
                        }
                    }
                }
                break;
            }
        }

        // 중복 제거를 위한 distinct 추가
        List<Item> content = queryFactory
                .selectFrom(item)
                .where(builder)
                .orderBy(item.regTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        long total = queryFactory
                .selectFrom(item)
                .where(builder)
                .distinct()
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Item> findItemsBySearchQuery(String searchQuery, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 검색어가 있는 경우
        if (searchQuery != null && !searchQuery.isEmpty()) {
            builder.and(item.itemName.containsIgnoreCase(searchQuery));
        }

        List<Item> content = queryFactory
                .selectFrom(item)
                .where(builder)
                .orderBy(item.regTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(item)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

}
