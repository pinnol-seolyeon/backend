package jpabasic.pinnolbe.domain.analyze;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "weekly_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyAnalysis {

    @Id
    private String id;

    private String userId;

    private LocalDate weekStartDate;

    private EngagementData engagementData;
    private FocusData focusData;
    private UnderstandingData understandingData;
    private ExpressionData expressionData;
    //주간 학습 시간대
    private WeeklyTimeZone weeklyTimeZone;

    private LocalDateTime analyzedAt;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EngagementData {
        private int questionCount;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FocusData {
        private double averageResponseTime;
        private double sumResponseTime;
        private int count;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnderstandingData {
        private int correct;
        private int total;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpressionData {
        private int starScore;
        private int starCount;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyTimeZone{
        List<DayTimeZone> dayTimeZones;
    }

    @Getter @Setter
    @AllArgsConstructor
    @Builder
    public static class DayTimeZone{
        private Map<String,Long> dayTimeZone;
        private LocalDate day;
        private DayOfWeek dayOfWeek;

        public DayTimeZone(LocalDate day){
            this.day=LocalDate.now();
            this.dayOfWeek=day.getDayOfWeek();
        }
    }

    public WeeklyAnalysis(String userId){
        this.userId = userId;
    }


}
