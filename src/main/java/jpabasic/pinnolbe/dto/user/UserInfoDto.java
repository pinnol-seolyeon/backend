package jpabasic.pinnolbe.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfoDto {
    private String userId;
    private String username;
    private String name;
    private int coin;


    public UserInfoDto(String userId,String username, String name, int reward) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.coin = reward;
    }

}
