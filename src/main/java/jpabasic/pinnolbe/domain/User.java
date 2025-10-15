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

    //가입자(아이)의 이름
    private String name;

    @Nullable
    private String studyId;

    @Nullable
    private String studySessionLogId; //최근 학습 상태(진도)

    //여태까지 모은 코인 개수
    private int reward=0;

    //부모 전화번호
    private String phoneNumber;

    //개인정보 수집 이용 동의여부
    @Nullable
    private Boolean agreement;

    public User(String studySessionLogId) {

    }

}
