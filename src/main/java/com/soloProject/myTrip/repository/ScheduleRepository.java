package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByItemId(Long itemId);

    Optional<Schedule> findByItemIdAndDay(Long itemId, int day);
}
