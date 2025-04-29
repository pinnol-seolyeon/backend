package jpabasic.pinnolbe.domain;

import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection="message")
@NoArgsConstructor
public class Message {

    @Id
    private String id;

    private String userId;

    private List<String> Messages;
}
