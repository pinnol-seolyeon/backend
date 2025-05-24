package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.Score;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.QuizResultDto;
import jpabasic.pinnolbe.dto.ScoreRequestDto;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.repository.ScoreRepository;
import jpabasic.pinnolbe.service.ScoreService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreRepository scoreRepository;
    private final UserService userService;
    private final ScoreService scoreService;

    public ScoreController(ScoreRepository scoreRepository, UserService userService, ScoreService scoreService) {
        this.scoreRepository = scoreRepository;
        this.userService = userService;
        this.scoreService = scoreService;
    }

    @PostMapping
    public ResponseEntity<?> saveScore(@RequestBody ScoreRequestDto request) {

        // ✅ 현재 로그인한 사용자 정보 가져오기
        User user = userService.getUserInfo();

        // ✅ 점수 객체 생성 및 사용자 정보 중복 저장
        Score score = new Score();
        score.setChildId(user.getId());       // _id (중요!)
        score.setChildName(user.getName());   // 중복 저장
        score.setUnit(request.getUnit());
        score.setScore(request.getScore());
        score.setCoin(request.getCoin());

        scoreRepository.save(score);
        return ResponseEntity.ok("Saved");
    }

    @GetMapping("/results")
    public ResponseEntity<List<ScoreRequestDto>> getMyScoreResults() {
        User user = userService.getUserInfo();  // 현재 로그인 사용자
        List<ScoreRequestDto> results = scoreService.getScore(user.getId());
        return ResponseEntity.ok(results);
    }
}
