package jpabasic.pinnolbe.repository.analyze;

import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyAnalysisRepository extends MongoRepository<WeeklyAnalysis, String> {
    List<WeeklyAnalysis> findAllByUserIdAndWeekStartDate(String userId, LocalDate weekStartDate);
}