package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.Sex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_reservation_id")
    private MemberReservation memberReservation;

    @Column(nullable = false)
    private String name;

    @Column(length = 6, nullable = false)
    private String birth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(length = 12)
    private String tel; //전화번호는 1인 필수

    //결제후 추가 정보

    private String engFirstName; //영문 이름
    private String engLastName; //영문 성
    private String passportNumber; //여권번호

}
