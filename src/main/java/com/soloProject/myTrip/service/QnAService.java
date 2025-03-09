package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.QnA;
import com.soloProject.myTrip.repository.MemberRepository;
import com.soloProject.myTrip.repository.QnARepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class QnAService {
    private final QnARepository qnARepository;
    private final MemberRepository memberRepository;

    public void saveQuestion(QnADto qnADto, String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        QnA qnA = qnADto.createQnA();
        qnA.setMember(member);
        qnARepository.save(qnA);

        //인당 QnA는 5개로 유지(최신순)
        List<QnA> allQuestions = qnARepository.findByMemberIdOrderByRegTimeDesc(member.getId());
        if(allQuestions.size() > 5){
            QnA oldestQnA = allQuestions.getLast();
            qnARepository.delete(oldestQnA);
        }
    }

    public void deleteQuestion(Long qnAId){
        QnA qnA = qnARepository.findById(qnAId).orElseThrow(EntityNotFoundException::new);
        qnARepository.delete(qnA);
        log.info("qna 삭제 완료");
    }

    public void saveAnswer(Long qnaId, String answer){
        QnA qnA = qnARepository.findById(qnaId).orElseThrow(EntityNotFoundException::new);
        qnA.updateAnswer(answer);
        log.info("qna 답변 등록 완료");
    }

    @Transactional(readOnly = true)
    public Page<QnA> getAdminQnAPage(Pageable pageable){
        return qnARepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<QnADto> getMemberQnAPage(String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        List<QnA> qnAs = qnARepository.findByMemberIdOrderByRegTimeDesc(member.getId());
        List<QnADto> qnADtos = new ArrayList<>();
        if(!qnAs.isEmpty()) {
            for (QnA qnA : qnAs) {
                qnADtos.add(QnADto.of(qnA));
            }
        }
        return qnADtos;
    }

    @Transactional(readOnly = true)
    public QnADto getQnADtl(Long qnaId){
        QnA qnA = qnARepository.findById(qnaId).orElseThrow(EntityNotFoundException::new);
        return QnADto.of(qnA);
    }

    @Transactional(readOnly = true)
    public QnADto getQnADtl(Long qnaId, String email){
        QnA qnA = qnARepository.findById(qnaId).orElseThrow(EntityNotFoundException::new);
        QnADto qnADto = QnADto.of(qnA);
        if(qnA.getMember().getEmail().equals(email)){
            qnADto.setAuthor(true);
        }
        return qnADto;
    }

    public void updateQnA(QnADto qnADto){
        QnA qnA = qnARepository.findById(qnADto.getId()).orElseThrow(EntityNotFoundException::new);
        qnA.updateQnA(qnADto);
    }

}
