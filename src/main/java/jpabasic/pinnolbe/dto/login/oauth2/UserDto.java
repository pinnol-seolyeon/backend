package jpabasic.pinnolbe.dto.login.oauth2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private String username;
    private String name;
    private String role;
    private String childName;
}
