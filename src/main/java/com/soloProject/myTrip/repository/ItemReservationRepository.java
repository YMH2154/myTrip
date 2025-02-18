package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemReservationRepository extends JpaRepository<ItemReservation, Long> {
  void deleteByItemAndReservationDate(Item item, LocalDate reservationDate);

  ItemReservation findByItemIdAndReservationDate(Long itemId, LocalDate reservationDate);

  List<ItemReservation> findByItemAndReservationDateBetween(
          Item item, LocalDate startDate, LocalDate endDate);

  List<ItemReservation> findByItemId(Long itemId);
}