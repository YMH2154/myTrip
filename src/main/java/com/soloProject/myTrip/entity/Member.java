package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.Provider;
import com.soloProject.myTrip.constant.Role;
import com.soloProject.myTrip.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

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

    @Column(length = 13)
    private String tel;

    @Column(length = 5)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 6)
    private Provider provider;

    private Integer mileage;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnA> qnAList;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private CouponWallet couponWallet;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setTel(memberFormDto.getTel());
        member.setMileage(1000); // 테스트 용 마일리지
        member.setRole(Role.ADMIN);
        member.setProvider(Provider.LOCAL); // 로컬 가입자

        return member;
    }

    public void update(String name) {
        this.name = name;
    }

    public void useMileage(int mileage) {
        if (this.mileage < mileage) {
            throw new IllegalStateException("보유 마일리지가 부족합니다.");
        }
        this.mileage -= mileage;
    }

    public void addMileage(int mileage) {
        this.mileage += mileage;
    }

    public void cancelMileage(int mileage){ this.mileage -= mileage; }
}
