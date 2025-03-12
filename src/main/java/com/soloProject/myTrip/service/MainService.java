package com.soloProject.myTrip.service;

import com.querydsl.core.BooleanBuilder;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.entity.QItem;
import com.soloProject.myTrip.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MainService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<Item> getRecommendedItems() {
        List<Item> recommendedItems = itemRepository.findRandomItems(1);
        setItemInfo(recommendedItems);

        return itemRepository.findRandomItems(1);
    }

    @Transactional(readOnly = true)
    public List<Item> getMostCheapItems() {
        // 추천 상품에서 제외된 상품들 중에서 가장 저렴한 상품 조회
        List<Item> recommendedItems = itemRepository.findRandomItems(1);
        Set<Long> recommendedIds = recommendedItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        // 특가 상품 조회 (추천 상품 제외)
        List<Item> cheapItems = itemRepository.findMostCheapItemsExcluding(PageRequest.of(0, 4), recommendedIds);

        // 상품 정보 설정
        setItemInfo(cheapItems);

        return cheapItems;
    }

    @Transactional(readOnly = true)
    public List<Item> getMostReservationCountItems() {
        // 추천 상품과 특가 상품에서 제외된 상품들 중에서 예약이 많은 상품 조회
        List<Item> recommendedItems = getRecommendedItems();
        List<Item> cheapItems = getMostCheapItems();

        Set<Long> excludedIds = new HashSet<>();
        excludedIds.addAll(recommendedItems.stream().map(Item::getId).collect(Collectors.toSet()));
        excludedIds.addAll(cheapItems.stream().map(Item::getId).collect(Collectors.toSet()));

        List<Item> reservationCountItems = itemRepository.findMostReservationCountItemsExcluding(PageRequest.of(0, 4),
                excludedIds);

        setItemInfo(reservationCountItems);

        return reservationCountItems;
    }

    // 메인 상품 (카테고리) 페이지 조회
    @Transactional(readOnly = true)
    public List<Item> getItemByCategory(String link, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "regTime"));
        Page<Item> itemPage = itemRepository.findItemsByCategory(link, pageRequest);

        List<Item> itemList = new ArrayList<>(itemPage.getContent());

        setItemInfo(itemList);

        return itemList;
    }

    // 메인 상품 (검색) 페이지 조회
    @Transactional(readOnly = true)
    public List<Item> getSearchItemPage(String searchQuery, int page, int pageSize) {
        QItem qItem = QItem.item;
        BooleanBuilder builder = new BooleanBuilder();

        // 검색어가 비어있지 않은 경우에만 검색 조건 추가
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            builder.and(qItem.itemName.containsIgnoreCase(searchQuery)
                    .or(qItem.domesticCategory.stringValue().containsIgnoreCase(searchQuery))
                    .or(qItem.overseasCategory.stringValue().containsIgnoreCase(searchQuery))
                    .or(qItem.themeCategory.stringValue().containsIgnoreCase(searchQuery)));
        }

        // 페이징 처리
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "regTime"));

        // QuerydslPredicateExecutor를 사용하여 검색 실행
        Page<Item> itemPage = itemRepository.findAll(builder, pageRequest);

        List<Item> itemList = new ArrayList<>(itemPage.getContent());

        setItemInfo(itemList);

        // Entity를 DTO로 변환
        return itemList;
    }

    public void setItemInfo(List<Item> items) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate endDate = tomorrow.plusDays(6);

        items.forEach(item -> {
            // 최초 출발일 설정
            LocalDate earliestDate = item.getReservations().stream()
                    .filter(reservation -> {
                        try {
                            LocalDate reservationDate = LocalDate
                                    .parse(reservation.getDepartureDateTime().split("T")[0]);
                            return !reservationDate.isBefore(tomorrow);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .map(reservation -> LocalDate.parse(reservation.getDepartureDateTime().split("T")[0]))
                    .min(LocalDate::compareTo)
                    .orElse(tomorrow);

            item.setEarliestDepartureDate(earliestDate);

            // 항공사 정보 설정
            String airline = item.getReservations().stream()
                    .filter(reservation -> reservation.getDepartureCarrierName() != null
                            && !reservation.getDepartureCarrierName().isEmpty())
                    .findFirst()
                    .map(ItemReservation::getDepartureCarrierName)
                    .orElse("대한항공"); // 기본값 설정

            item.setAirline(airline);

            // 내일부터 6일 동안의 예약 가격 중 최저가 계산
            int lowestPrice = item.getReservations().stream()
                    .filter(reservation -> {
                        try {
                            LocalDate reservationDate = LocalDate
                                    .parse(reservation.getDepartureDateTime().split("T")[0]);
                            return !reservationDate.isBefore(tomorrow) && !reservationDate.isAfter(endDate);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .mapToInt(ItemReservation::getTotalPrice)
                    .min()
                    .orElse(item.getPrice()); // 예약이 없는 경우 기본 가격 사용

            // 최저가가 0인 경우 기본 가격 사용
            item.setLowestPrice(lowestPrice > 0 ? lowestPrice : item.getPrice());
        });
    }
}
