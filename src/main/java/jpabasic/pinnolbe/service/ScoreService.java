package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Score;
import jpabasic.pinnolbe.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ScoreRepository scoreRepository;

    public void saveScore(String childId, int score) {
        Score s = Score.builder()
                .childId(childId)
                .score(score)
                .build();
        scoreRepository.save(s);
    }
}
