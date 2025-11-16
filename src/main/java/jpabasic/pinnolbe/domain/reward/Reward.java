package jpabasic.pinnolbe.domain.reward;

import jpabasic.pinnolbe.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="reward")
@CompoundIndex(
        name="idx_userId_createdAt",
        def="{'userId':1,'createdAt':-1}"
)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Reward extends BaseEntity {

    @Id
    private String id;
    //유저 기준으로 조회가 매우 많이 일어나므로 인덱스 필수
    @Indexed
    private String userId;
    private Integer coin;
    private PointCategory category;
    private String description;
    private boolean isPositive;

}
