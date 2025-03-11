package com.soloProject.myTrip.aop;

import com.soloProject.myTrip.entity.QnA;
import com.soloProject.myTrip.repository.QnARepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class QnAAccessAspect {
    private final QnARepository qnARepository;

    @Before("@annotation(com.soloProject.myTrip.annotation.CehckQnAAccess) && args(qnaId,..)")
    public void checkAccess(JoinPoint joinPoint, Long qnaId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        QnA qnA = qnARepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다."));

        if(!qnA.getMember().getEmail().equals(currentUserEmail)){
            throw new AccessDeniedException("해당 Q&A에 대한 접근 권한이 없습니다.");
        }
    }
}
