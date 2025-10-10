package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreDto;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class RadarChartController {

    private final RadarScoreService radarScoreService;

    public RadarChartController(RadarScoreService radarScoreService) {
        this.radarScoreService = radarScoreService;
    }

    @GetMapping("/radar-score")
    @Operation(summary="engagement/focus/understanding/expression 학습분석 내용 제공(수정 전)")
    public ResponseEntity<RadarScoreDto> getRadarScore() {
        return ResponseEntity.ok(radarScoreService.getThisWeekRadarScore());
    }

    // 지난주 데이터까지
    @GetMapping("/radar-score/compare")
    @Operation(summary="학습 분석 지난주와 비교(수정 전)")
    public ResponseEntity<RadarScoreComparisonDto> getRadarComparison() {
        return ResponseEntity.ok(radarScoreService.getThisAndLastWeekRadarScore());
    }
}
