package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    @Query("SELECT i FROM Item i WHERE i.currentParticipants < i.remainingSeats ORDER BY (i.remainingSeats - i.currentParticipants) ASC")
    List<Item> findTop8ByOrderByRemainingSeatsAsc(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.currentParticipants >= i.minParticipants ORDER BY i.id DESC")
    List<Item> findTop8ByParticipantsCondition(Pageable pageable);
}