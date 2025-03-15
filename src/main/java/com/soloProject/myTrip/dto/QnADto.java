package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.QuestionType;
import com.soloProject.myTrip.entity.QnA;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Getter
@Setter
public class QnADto {
    private Long id;

    @Length(max = 50, message = "제목은 50자 이하로 입력해주세요")
    private String title;

    private QuestionType questionType;

    @Length(max = 255, message = "내용은 최대 255자 이하로 입력해주세요.")
    private String content;

    private boolean isAnswered;

    @Length(max = 255, message = "답변은 최대 255자 이하로 입력해주세요.")
    private String answer;

    private LocalDateTime regTime;

    private boolean author;

    public static ModelMapper modelMapper = new ModelMapper();

    public static QnADto of(QnA qnA){
        return modelMapper.map(qnA, QnADto.class);
    }

    public QnA createQnA(){
        return modelMapper.map(this, QnA.class);
    }

}
