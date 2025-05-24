package jpabasic.pinnolbe.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    private String username;
//    private String name;
//    private String role;
    private String childName;
    private int coin;


}
