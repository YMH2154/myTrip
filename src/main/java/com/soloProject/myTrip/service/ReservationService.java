package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.exception.NotEnoughRemainingSeats;
import com.soloProject.myTrip.repository.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
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
    private final ItemRepository itemRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public MemberReservation createReservation(Long itemId, String departureDateTime,
            List<ParticipantDto> participants, String email, int totalDeposit, int totalPrice, String bookerTel) {

        ItemReservation itemReservation = itemReservationRepository
                .findByItemIdAndDepartureDateTime(itemId, departureDateTime);

        int adultAndChildCount = (int) participants.stream().filter(p -> p.getAge() != Age.INFANT).count();

        // 1. 잔여 좌석 비교
        if (adultAndChildCount > itemReservation.getRemainingSeats()) {
            throw new NotEnoughRemainingSeats("잔여 좌석이 부족합니다.");
        }

        // 2. 필요한 엔티티 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityExistsException("회원을 찾을 수 없습니다."));

        // 회원 전화번호가 없는 경우 업데이트
        if (member.getTel() == null || member.getTel().isEmpty()) {
            member.setTel(bookerTel);
            memberRepository.save(member);
        }

        Item item = itemRepository.findById(itemReservation.getItem().getId()).orElseThrow(EntityExistsException::new);
        item.plusReservationCount();

        // 3. 예약 번호 생성 (중복 검사 포함)
        String reservationNumber = generateUniqueReservationNumber();

        // 4. MemberReservation 생성
        MemberReservation memberReservation = new MemberReservation();
        memberReservation.setReservationNumber(reservationNumber);
        memberReservation.setItemReservation(itemReservation);
        memberReservation.setReservationStatus(ReservationStatus.RESERVED);
        memberReservation.setTotalDeposit(totalDeposit);
        memberReservation.setTotalPrice(totalPrice);
        memberReservation.setMember(member);

        // 5. Participant 목록 생성
        List<Participant> participantEntities = participants.stream()
                .map(dto -> createParticipant(memberReservation, dto))
                .collect(Collectors.toList());

        memberReservation.setParticipants(participantEntities);

        // 6. 저장 및 좌석 수 업데이트
        MemberReservation savedReservation = memberReservationRepository.save(memberReservation);
        updateRemainingSeats(itemReservation, adultAndChildCount);

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

    @Transactional
    public void cancelReservation(String reservationNumber, Principal principal) {
        // 1. 예약 정보 조회
        MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));

        // 2. 권한 확인 (시스템 취소인 경우 스킵)
        if (principal != null && !reservation.getMember().getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 3. 상품 예약 정보 조회
        ItemReservation itemReservation = itemReservationRepository
                .findById(reservation.getItemReservation().getId())
                .orElseThrow(() -> new EntityNotFoundException("상품 예약 정보를 찾을 수 없습니다."));

        // 4. 성인/아동 좌석 수 계산
        List<Participant> adult = participantRepository.findByMemberReservationIdAndAge(
                reservation.getId(), Age.ADULT);
        List<Participant> child = participantRepository.findByMemberReservationIdAndAge(
                reservation.getId(), Age.CHILD);
        int cancelledSeats = adult.size() + child.size();

        // 5. 좌석 수 복구
        int updatedRemainingSeats = itemReservation.getRemainingSeats() + cancelledSeats;
        itemReservation.updateRemainingSeats(updatedRemainingSeats);

        // 6. 예약 상태 변경
        reservation.updateStatus(ReservationStatus.RESERVATION_CANCELLED);

        // 7. 상품의 예약 카운트 감소
        Item item = itemRepository.findById(itemReservation.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        item.minusReservationCount();

        log.info("예약 취소 처리 완료 - 예약번호: {}, 취소된 좌석: {}, 갱신된 잔여좌석: {}",
                reservationNumber, cancelledSeats, updatedRemainingSeats);
    }
}
