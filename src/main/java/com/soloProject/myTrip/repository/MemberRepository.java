package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Member findByTel(String tel);
}
