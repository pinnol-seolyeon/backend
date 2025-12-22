package jpabasic.pinnolbe.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final RadarScoreService radarScoreService;

	// 지난주 데이터까지
	@GetMapping("/radar-score")
	@Operation(summary="특정 유저의 weekly_analysis 내용 제공")
	public ApiResponse<RadarScoreComparisonDto> getRadarComparison(@RequestParam String userId) {
		return ApiResponse.success("이번 주 이해도 분석 내용입니다.",radarScoreService.getRadarScore(userId));
	}

}
