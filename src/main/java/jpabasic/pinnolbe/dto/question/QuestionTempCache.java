package jpabasic.pinnolbe.dto.question;

import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class QuestionTempCache {

    // key: userId, value: 질문/응답 리스트
    private final Map<String, List<TempQA>> cache = new ConcurrentHashMap<>();

    @Getter
    @AllArgsConstructor
    public static class TempQA {
        private final String question;
        private final String answer;
        private final Instant createdAt;
    }

    // ✅ Q/A 한 쌍 추가
    public void add(String userId, String question, String answer) {
        cache.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(new TempQA(question, answer, Instant.now()));
    }

    // ✅ 해당 유저의 모든 Q/A 조회
    public List<TempQA> getAll(String userId) {
        return cache.getOrDefault(userId, Collections.emptyList());
    }

    // ✅ 해당 유저의 Q/A 모두 꺼내고 삭제
    public List<TempQA> popAll(String userId) {
        return cache.remove(userId);
    }

    // ✅ 오래된 세션 정리용
    public void cleanupOlderThan(Instant cutoff) {
        cache.forEach((userId, list) ->
                list.removeIf(item -> item.getCreatedAt().isBefore(cutoff))
        );
    }
}
