package com.soloProject.myTrip.service;


import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.MemberUpdateFormDto;
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
        if(memberRepository.findByTel(memberFormDto.getTel()).isPresent()){
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
                .findByMemberOrderByRegTimeDesc(member);
    }

    @Transactional(readOnly = true)
    public MemberReservation getReservationByNumber(String number){
        return memberReservationRepository
                .findByReservationNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));
    }

    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
//      임시막음
//      if (newPassword.length() < 8 || !newPassword.matches(".*[A-Za-z].*") || !newPassword.matches(".*[0-9].*")) {
//         throw new IllegalArgumentException("새 비밀번호는 8자 이상이며, 영문과 숫자를 포함해야 합니다.");
//      }

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public void updateMember(MemberUpdateFormDto memberUpdateFormDto, Long id) {

        Member member = memberRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        member.updateMember(memberUpdateFormDto);
    }
}
