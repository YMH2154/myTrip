package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.QuestionType;
import com.soloProject.myTrip.entity.QnA;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class QnADto {
    private Long id;

    private String title;

    private QuestionType questionType;

    private String content;

    private boolean isAnswered;

    private String answer;

    private LocalDateTime regTime;

    public static ModelMapper modelMapper = new ModelMapper();

    public static QnADto of(QnA qnA){
        return modelMapper.map(qnA, QnADto.class);
    }

    public QnA createQnA(){
        return modelMapper.map(this, QnA.class);
    }

}
