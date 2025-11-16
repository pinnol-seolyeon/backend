package jpabasic.pinnolbe.domain.reward;

import jpabasic.pinnolbe.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="reward")
@CompoundIndex(name="idx_userId_createdAt",def="{'userId':1,'createdAt':-1}")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Reward extends BaseEntity {

    @Id
    private String id;
    private String userId;
    private Integer coin;
    private PointCategory category;
    private String description;
    private boolean isPositive;

}
