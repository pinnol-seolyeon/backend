package jpabasic.pinnolbe.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="user")
@RequiredArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
public class User {
    @Id
    private String id;
    //자녀 이름
    private String username;
    //자녀 나이
    private Long age;

    private String email;

    private String role;

    private String name;



    public User(String username,Long age){
        this.username=username;
        this.age=age;
    }

//    public User(String email,String username,String accessToken){
//        this.username=username;
//        this.accessToken=accessToken;
//        this.email=email;
//    }
}
