package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository
        extends JpaRepository<Item, Long>, ItemRepositoryCustom, QuerydslPredicateExecutor<Item> {
    // 랜덤으로 8개 상품 조회
    @Query(value = "SELECT * FROM item ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Item> findRandomItems(@Param("limit") int limit);

    @Query("SELECT i FROM Item i ORDER BY i.lowestPrice ASC")
    List<Item> findMostCheapItems(Pageable pageable);

    @Query("SELECT i FROM Item i ORDER BY i.reservationCount DESC")
    List<Item> findMostReservationCountItems(Pageable pageable);

}