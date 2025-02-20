package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.MemberReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {
    List<MemberReservation> findByMemberId(Long memberId);
} 