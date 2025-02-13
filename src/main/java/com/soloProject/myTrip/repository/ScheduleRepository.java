package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByItemId(Long itemId);
}
