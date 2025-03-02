package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom, QuerydslPredicateExecutor<Item> {
    @Query("SELECT i FROM Item i WHERE i.currentParticipants >= i.minParticipants ORDER BY i.id DESC")
    List<Item> findTop8ByParticipantsCondition(Pageable pageable);

    // 랜덤으로 8개 상품 조회
    @Query(value = "SELECT * FROM item ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Item> findRandomItems(int limit);

}