package com.soloProject.myTrip.repository;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    private JPAQueryFactory queryFactory;
    
    @PersistenceContext
    private EntityManager entityManager;

    public ItemRepositoryCustomImpl(EntityManager entityManager){
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    private BooleanExpression searchTravelTypeEq(TravelType travelType){
        return travelType == null
                ? null
                : QItem.item.travelType.eq(travelType);
    }

    private BooleanExpression searchOverseasCategory(OverseasCategory overseasCategory){
        return overseasCategory == null
                ? null
                : QItem.item.overseasCategory.eq(overseasCategory);
    }

    private BooleanExpression searchDomesticCategory(DomesticCategory domesticCategory){
        return domesticCategory == null
                ? null
                : QItem.item.domesticCategory.eq(domesticCategory);
    }

    private BooleanExpression searchThemeCategory(ThemeCategory themeCategory){
        return themeCategory == null
                ? null
                : QItem.item.themeCategory.eq(themeCategory);
    }

    private BooleanExpression regDtsAfter(String searchDateType){
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all",searchDateType) || searchDateType == null){
            return null;
        }
        else if(StringUtils.equals("1d",searchDateType)){
            dateTime = dateTime.minusDays(1);
        }
        else if(StringUtils.equals("1W",searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        }
        else if(StringUtils.equals("1m",searchDateType)){
            dateTime = dateTime.minusMonths(1);
        }
        else if(StringUtils.equals("6m",searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }
        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if(StringUtils.equals("itemName", searchBy)){
            return QItem.item.itemName.like("%"+searchQuery+"%");
        }
        else if(StringUtils.equals("createdBy",searchBy)){
            return QItem.item.createdBy.like("%"+searchQuery+"%");
        }
        else {
            return null;
        }
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QueryResults<Item> results =queryFactory.selectFrom(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType())
                        ,searchTravelTypeEq(itemSearchDto.getTravelType())
                        ,searchOverseasCategory(itemSearchDto.getOverseasCategory())
                        ,searchDomesticCategory(itemSearchDto.getDomesticCategory())
                        ,searchThemeCategory(itemSearchDto.getThemeCategory())
                        ,searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();

        List<Item> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }
}
