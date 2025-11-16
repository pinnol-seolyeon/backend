package jpabasic.pinnolbe.domain.badge;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="badge")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Badge {
    @Id
    private String id;
    private String chapterId;
    private String userId;
    private BadgeType badgeType;

}
