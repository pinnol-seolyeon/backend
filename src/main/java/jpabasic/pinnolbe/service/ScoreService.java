package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Score;
import jpabasic.pinnolbe.dto.QuizResultDto;
import jpabasic.pinnolbe.dto.ScoreRequestDto;
import jpabasic.pinnolbe.repository.QuizResultRepository;
import jpabasic.pinnolbe.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ScoreRepository scoreRepository;

    public List<ScoreRequestDto> getScore(String childId) {
        return scoreRepository.findByChildId(childId).stream()
                .map(result -> new ScoreRequestDto(
                        result.getUnit(),
                        result.getScore(),
                        result.getCoin()
                )).collect(Collectors.toList());
    }
}
