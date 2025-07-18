package jpabasic.pinnolbe.domain;

import com.mongodb.lang.Nullable;
import jpabasic.pinnolbe.domain.study.Study;
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
    //암호화된 name
    private String username;

    private String email;

    private String role;
    //가입자(부모)의 이름
    private String name;
    //카카오에서 발급한 AccessToken
    private String accessToken;

    @Nullable
    private String studyId;

    //여태까지 모은 코인 개수
    private int reward=0;


    //별도로 입력받아야 할 자녀 정보
    private String childName;
    //자녀 나이
    private int childAge;
    //부모 전화번호
    private String phoneNumber;







//    public User(String username,Long age){
//        this.username=username;
//        this.age=age;
//    }

//    public User(String email,String username,String accessToken){
//        this.username=username;
//        this.accessToken=accessToken;
//        this.email=email;
//    }
}
