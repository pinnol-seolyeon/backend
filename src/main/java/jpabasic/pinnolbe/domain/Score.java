package jpabasic.pinnolbe.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scores")
public class Score {
    @Id
    private String id;

    private String userId;
    private int score;
    private String timestamp;

    public Score() {}

    public Score(String userId, int score, String timestamp) {
        this.userId = userId;
        this.score = score;
        this.timestamp = timestamp;
    }

    // getters/setters
}
