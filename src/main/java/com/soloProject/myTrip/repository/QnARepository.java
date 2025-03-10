package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.QnA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnARepository extends JpaRepository<QnA, Long> {
    Page<QnA> findAll(Pageable pageable);

    List<QnA> findByMemberIdOrderByRegTimeDesc(Long memberId);

    Long countByIsAnsweredFalse();
}
