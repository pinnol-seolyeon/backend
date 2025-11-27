package jpabasic.pinnolbe.dto.review;

public record QuizRecordForAI(
	String question,
	String userAnswer,
	Boolean isCorrect,
	String description
) {
}


