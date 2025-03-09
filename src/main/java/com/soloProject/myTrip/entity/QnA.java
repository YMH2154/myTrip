package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.QuestionType;
import com.soloProject.myTrip.dto.QnADto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class QnA extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(nullable = false)
    private String content;

    private boolean isAnswered;

    private String answer;

    public void updateAnswer(String answer){
        this.answer = answer;
        this.isAnswered = true;
    }

    public void updateQnA(QnADto qnADto){
        this.title = qnADto.getTitle();
        this.questionType = qnADto.getQuestionType();
        this.content = qnADto.getContent();
    }

}
