package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.constant.Sex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Participant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_reservation_id")
    private MemberReservation memberReservation;

    @Column(nullable = false)
    private String name;

    @Column(length = 8, nullable = false)
    private String birth;

    @Column(length = 6, nullable = false)
    @Enumerated(EnumType.STRING)
    private Age age;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(length = 12)
    private String tel; //전화번호는 1인 필수

}
