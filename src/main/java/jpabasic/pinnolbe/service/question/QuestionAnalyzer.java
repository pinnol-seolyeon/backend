package jpabasic.pinnolbe.service.question;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.time.DateUtils.round;
@Component
public class QuestionAnalyzer {


    // ✅ 5점 정규화 메서드
    public static double scaleToFive(double value, double minVal, double maxVal) {
        double normalized = (value - minVal) / (maxVal - minVal);
        double score = 1.0 + (normalized * 4.0);
        return Math.max(1.0, Math.min(5.0, score));
    }

    // ✅ 핵심 점수 계산 로직
    public static double calculateScores(List<String> questions) {

        if (questions == null || questions.isEmpty()) return 1.0;

        int totalEojeols = 0;
        Set<String> allEojeolsSet = new HashSet<>();
        Pattern spacePattern = Pattern.compile("\\s+");

        for (String q : questions) {
            if (q == null || q.trim().isEmpty()) continue;

            // 어절 단위로 분리
            String[] eojeols = spacePattern.split(q.trim());
            List<String> validEojeols = Arrays.stream(eojeols)
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());

            int currentEojeolCount = validEojeols.size();
            totalEojeols += currentEojeolCount;
            allEojeolsSet.addAll(validEojeols);

            System.out.println("  '" + q + "' -> " + currentEojeolCount + "개 어절 분석 완료");
        }

        System.out.println("[ 질문 분석 완료. ]");

        int numQuestions = questions.size();

        if (totalEojeols == 0) return 1.0;

        // --- 지표 계산 ---

        // 1️⃣ 평균 어절 수
        double avgLength = (double) totalEojeols / numQuestions;
        double lengthScore = scaleToFive(avgLength, 1.0, 5.0);

        // 2️⃣ 어휘 다양성 비율
        double diversityRatio = (double) allEojeolsSet.size() / totalEojeols;
        double diversityScore = scaleToFive(diversityRatio, 0.1, 0.8);

        // --- 최종 점수 ---
        double finalScore = (lengthScore + diversityScore) / 2.0;

        // 디버그용 정보 구성
        Map<String, Object> debugInfo = new LinkedHashMap<>();
        debugInfo.put("totalQuestions", numQuestions);
        debugInfo.put("totalEojeols", totalEojeols);
        debugInfo.put("uniqueEojeols", allEojeolsSet.size());
        debugInfo.put("diversityRatio", String.format("%.1f%%", diversityRatio * 100));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("lengthScore(Avg_Eojeol)", round(lengthScore, 2));
        details.put("diversityScore(Unique_Ratio)", round(diversityScore, 2));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalScore", round(finalScore, 2));
        response.put("details", details);
        response.put("debug_info", debugInfo);

        return finalScore;
    }

    // ✅ 소수점 반올림
    private static double round(double value, int digits) {
        double factor = Math.pow(10, digits);
        return Math.round(value * factor) / factor;
    }

}
