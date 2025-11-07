package jpabasic.pinnolbe.domain.analyze;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Field("engagementData")
    private EngagementData engagementData;//참여도
    @Field("focusData")
    private FocusData focusData;//집중도
    @Field("understandingData")
    private UnderstandingData understandingData;//이해도
    @Field("expressionData")
    private ExpressionData expressionData;//표현력
    private WeeklyTimeZone weeklyTimeZone;//주간 학습 시간대+학습 시간(추가된 내용)

    private List<String> completedChapters=new ArrayList<>(); //학습완료한 chapter들

    private LocalDateTime analyzedAt;



    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EngagementData {
        private int questionCount=0;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FocusData {
        private double focusingScore; //weeklyAnalysis에는 5점에서 감점이 될 점수가 저장됨.

        public void setFocusingScore(double focusingScore) {
            if (focusingScore < 0){
                this.focusingScore=0;
            }else{
                this.focusingScore=focusingScore;
            }
        }
    }

    @Getter @Setter
    @AllArgsConstructor
    @Builder
    public static class UnderstandingData {
        private int correct;
        private int total;

        public UnderstandingData(){
            this.correct=0;
            this.total=0;
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpressionData {
        private double expressionScore=0;
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
    @NoArgsConstructor
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

    public WeeklyAnalysis(String userId,LocalDate weekStartDate){
        this.userId = userId;
        this.weekStartDate = weekStartDate;
        this.completedChapters=new ArrayList<>();
        this.engagementData=new EngagementData();
        this.focusData=new FocusData();
        this.understandingData=new UnderstandingData();
        this.expressionData=new ExpressionData();
    }




}
