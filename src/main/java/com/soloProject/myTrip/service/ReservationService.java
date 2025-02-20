package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.constant.Sex;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.entity.Participant;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.repository.MemberRepository;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    private final ItemReservationRepository itemReservationRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createReservation(Long itemId, String departureDateTime, 
                                List<String> names, List<String> births, 
                                List<String> sexes, String email) {
        // 1. 필요한 엔티티 조회
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityExistsException::new);
        ItemReservation itemReservation = itemReservationRepository
                .findByItemIdAndDepartureDateTime(itemId, departureDateTime);

        if (itemReservation == null) {
            throw new RuntimeException("해당 예약 정보를 찾을 수 없습니다.");
        }

        // 2. MemberReservation 생성
        MemberReservation memberReservation = new MemberReservation();
        memberReservation.setMember(member);
        memberReservation.setItemReservation(itemReservation);
        memberReservation.setReservationStatus(ReservationStatus.RESERVED);
        
        // 3. Participant 목록 생성
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Participant participant = new Participant();
            participant.setMemberReservation(memberReservation);
            participant.setName(names.get(i));
            participant.setBirth(births.get(i));
            participant.setSex(Sex.valueOf(sexes.get(i)));
            
            participants.add(participant);
        }
        
        memberReservation.setParticipants(participants);
        
        // 4. 저장
        memberReservationRepository.save(memberReservation);
        
        // 5. 잔여 좌석 업데이트 (성인 + 아동만 카운트)
        int adultAndChildCount = (int) participants.stream()
            .filter(p -> !p.getBirth().startsWith(LocalDate.now().minusYears(2).format(DateTimeFormatter.ofPattern("yy"))))
            .count();
        
        updateRemainingSeats(itemReservation, adultAndChildCount);
    }

    private void updateRemainingSeats(ItemReservation itemReservation, int count) {
        int remainingSeats = itemReservation.getRemainingSeats() - count;
        if (remainingSeats < 0) {
            throw new RuntimeException("잔여 좌석이 부족합니다.");
        }
        itemReservation.updateRemainingSeats(remainingSeats);
    }
}
