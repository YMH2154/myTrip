package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.AirportCode;
import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@Entity
@Table(name = "member")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private String name;
    private String tel;

    @Column(length = 5)
    @Enumerated(EnumType.STRING)
    private Role role;

    private Integer loginCount;

    private String provider;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setTel(memberFormDto.getTel());
        member.setRole(Role.ADMIN);

        return member;
    }

    public Member update(String name) {
        this.name = name;
        return this;
    }
}
