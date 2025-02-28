package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.MemberUpdateFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

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

    private String provider;

    private Integer mileage;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private CouponWallet couponWallet;


    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setTel(memberFormDto.getTel());
        member.setMileage(0);
        member.setRole(Role.ADMIN);
        member.setProvider("local"); //로컬 가입자

        return member;
    }

    public void update(String name) {
        this.name = name;
    }

    public void updateMember(MemberUpdateFormDto memberUpdateFormDto) {
        this.tel = memberUpdateFormDto.getTel();
    }
}
