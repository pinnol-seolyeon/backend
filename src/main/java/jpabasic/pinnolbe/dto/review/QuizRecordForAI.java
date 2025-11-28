package jpabasic.pinnolbe.dto.review;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

public record QuizRecordForAI(
	Long quizId,
	String question,
	String userAnswer,
	Boolean isCorrect,
	String correctAnswer,
	String description,
	List<String> options,
	String quizDate
) {

	public QuizRecordForAI(Long quizId, String question, String userAnswer,
		Boolean isCorrect, String correctAnswer,String description,List<String> options) {

		this(
			quizId,
			question,
			userAnswer,
			isCorrect,
			correctAnswer,
			description,
			options,
			LocalDate.now().toString()    // quizDate 기본값
		);
	}


}


