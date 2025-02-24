package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.ReservationStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.soloProject.myTrip.dto.ParticipantDto;
import com.soloProject.myTrip.constant.Age;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    private final ItemReservationRepository itemReservationRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public MemberReservation createReservation(Long itemId, String departureDateTime,
            List<ParticipantDto> participants, String email, int totalDeposit, int totalPrice, String bookerTel) {
        // 1. 필요한 엔티티 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityExistsException("회원을 찾을 수 없습니다."));

        // 회원 전화번호가 없는 경우 업데이트
        if (member.getTel() == null || member.getTel().isEmpty()) {
            member.setTel(bookerTel);
            memberRepository.save(member);
        }

        ItemReservation itemReservation = itemReservationRepository
                .findByItemIdAndDepartureDateTime(itemId, departureDateTime);

        if (itemReservation == null) {
            throw new RuntimeException("해당 예약 정보를 찾을 수 없습니다.");
        }

        // 2. 예약 번호 생성 (중복 검사 포함)
        String reservationNumber = generateUniqueReservationNumber();

        // 3. MemberReservation 생성
        MemberReservation memberReservation = new MemberReservation();
        memberReservation.setReservationNumber(reservationNumber);
        memberReservation.setItemReservation(itemReservation);
        memberReservation.setReservationStatus(ReservationStatus.RESERVED);
        memberReservation.setTotalDeposit(totalDeposit);
        memberReservation.setTotalPrice(totalPrice);
        memberReservation.setMember(member);

        // 4. Participant 목록 생성
        List<Participant> participantEntities = participants.stream()
                .map(dto -> createParticipant(memberReservation, dto))
                .collect(Collectors.toList());

        memberReservation.setParticipants(participantEntities);

        // 5. 저장
        MemberReservation savedReservation = memberReservationRepository.save(memberReservation);

        // 6. 잔여 좌석 업데이트 (성인 + 아동만 카운트)
        long adultAndChildCount = participants.stream()
                .filter(p -> p.getAge() != Age.INFANT)
                .count();

        updateRemainingSeats(itemReservation, (int) adultAndChildCount);

        return savedReservation;
    }

    private Participant createParticipant(MemberReservation reservation, ParticipantDto dto) {
        Participant participant = new Participant();
        participant.setMemberReservation(reservation);
        participant.setName(dto.getName());
        participant.setBirth(dto.getBirth());
        participant.setSex(dto.getSex());
        participant.setAge(dto.getAge());
        if (!dto.getTel().isEmpty()) {
            participant.setTel(dto.getTel());
        }
        return participant;
    }

    private String generateUniqueReservationNumber() {
        String reservationNumber = "";
        boolean isUnique = false; // 중복 확인 변수

        // 고유한 예약 번호를 찾을 때까지 반복
        while (!isUnique) {
            reservationNumber = createReservationNumber(); // 새 예약 번호 생성

            // 예약 번호 중복 체크
            Optional<MemberReservation> existingReservation = memberReservationRepository
                    .findByReservationNumber(reservationNumber);

            if (existingReservation.isEmpty()) {
                isUnique = true; // 중복되지 않으면 고유하므로 종료
            }
        }

        return reservationNumber;
    }

    private String createReservationNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
        return datePart + randomPart;
    }

    private void updateRemainingSeats(ItemReservation itemReservation, int count) {
        int remainingSeats = itemReservation.getRemainingSeats() - count;
        if (remainingSeats < 0) {
            throw new RuntimeException("잔여 좌석이 부족합니다.");
        }
        itemReservation.updateRemainingSeats(remainingSeats);
    }
}
