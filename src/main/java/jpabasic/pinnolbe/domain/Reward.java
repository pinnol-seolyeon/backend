package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="reward")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Reward {

    @Id
    private String id;

    private String userId;

    private Long coin;

}
