package jpabasic.pinnolbe.dto.question;

import jpabasic.pinnolbe.domain.question.QueCollection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QueCollectionResponseDto {

    private List<String> questions;
    private List<String> answers;

    public static QueCollectionResponseDto from(QueCollection entity) {
        QueCollectionResponseDto dto = new QueCollectionResponseDto();
        dto.setQuestions(entity.getQuestions());
        dto.setAnswers(entity.getAnswers());
        return dto;
    }

    public static List<QueCollectionResponseDto> fromList(List<QueCollection> entities) {
        return entities.stream()
                .map(QueCollectionResponseDto::from)
                .collect(Collectors.toList());
    }
}
