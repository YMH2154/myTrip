package com.soloProject.myTrip.service;


import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberRepository;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberReservationRepository memberReservationRepository;

    private void checkDuplicatedMember(MemberFormDto memberFormDto){
        if(memberRepository.findByEmail(memberFormDto.getEmail()).isPresent()){ //isPresent() -> Optional 값이 null이면 false, 아니면 true
            throw new IllegalStateException("이미 가입된 이메일입니다");
        }
        if(memberRepository.findByTel(memberFormDto.getTel()) != null){
            throw new IllegalStateException("이미 가입된 전화번호입니다");
        }
    }

    public void saveMember(MemberFormDto memberFormDto){
        checkDuplicatedMember(memberFormDto);
        Member member = Member.createMember(memberFormDto, passwordEncoder);
        member.setRole(Role.ADMIN); //Role 기본설정 (관리자)
        member.setLoginCount(0); //로그인 카운트 0
        member.setProvider("local"); //로컬 가입자
        memberRepository.save(member);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(EntityNotFoundException::new);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder().username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public MemberFormDto getMember(String email){
        return MemberFormDto.of(memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public List<MemberReservation> getReservations(String email){
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        return memberReservationRepository
                .findByMemberOrderByReservationNumberDesc(member);
    }

    @Transactional(readOnly = true)
    public MemberReservation getReservationByNumber(String number){
        return memberReservationRepository
                .findByReservationNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));
    }
}
