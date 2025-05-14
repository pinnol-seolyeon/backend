package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.Score;
import jpabasic.pinnolbe.dto.ScoreDto;
import jpabasic.pinnolbe.repository.ScoreRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreRepository scoreRepository;

    public ScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @PostMapping
    public ResponseEntity<String> saveScore(@RequestBody ScoreDto dto) {
        Score score = new Score(dto.getUserId(), dto.getScore(), dto.getTimestamp());
        scoreRepository.save(score);
        return ResponseEntity.ok("Score saved");
    }
}

