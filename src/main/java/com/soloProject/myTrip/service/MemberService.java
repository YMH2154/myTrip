package com.soloProject.myTrip.service;


import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

}
