package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.TouristAttraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImageRepository extends JpaRepository<TouristAttraction, Long> {
  List<TouristAttraction> findByItemIdOrderByIdAsc(Long itemId);
}