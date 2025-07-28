package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    private String id;
    private String token;
    private String username;


    public RefreshToken(String refreshToken,String username) {
        this.token = refreshToken;
        this.username = username;
    }
}
