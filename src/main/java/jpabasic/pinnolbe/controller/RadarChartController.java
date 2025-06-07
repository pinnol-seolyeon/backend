package jpabasic.pinnolbe.controller;

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
    public ResponseEntity<RadarScoreDto> getRadarScore() {
        return ResponseEntity.ok(radarScoreService.getThisWeekRadarScore());
    }

    // 지난주 데이터까지
    @GetMapping("/radar-score/compare")
    public ResponseEntity<RadarScoreComparisonDto> getRadarComparison() {
        return ResponseEntity.ok(radarScoreService.getThisAndLastWeekRadarScore());
    }
}
