package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {
    Optional<MemberReservation> findByReservationNumber(String number);

    List<MemberReservation> findByMemberOrderByReservationNumberDesc(Member member);
} 