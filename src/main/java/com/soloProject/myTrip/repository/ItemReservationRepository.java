package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemReservationRepository extends JpaRepository<ItemReservation, Long> {
  void deleteByItemAndDepartureDateTime(Item item, String departureDateTime);

  ItemReservation findByItemIdAndDepartureDateTime(Long itemId, String departureDateTime);

  List<ItemReservation> findByItemId(Long itemId);
}