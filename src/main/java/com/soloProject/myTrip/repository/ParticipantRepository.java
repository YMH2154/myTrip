package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByMemberReservationIdAndAge(Long reservationId, Age age);
}
