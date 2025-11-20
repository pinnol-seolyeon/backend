package jpabasic.pinnolbe.dto.quiz;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum QuizType {
    MAIN_STUDY("학습하기 퀴즈"),
    FIRST_REVIEW("1차 복습 퀴즈"),
    SECOND_REVIEW("2차 복습 퀴즈");

    private final String description;

}
