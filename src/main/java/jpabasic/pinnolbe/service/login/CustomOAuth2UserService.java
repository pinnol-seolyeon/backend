package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.dto.login.oauth2.KakaoResponse;
import jpabasic.pinnolbe.dto.login.oauth2.OAuth2Response;
import jpabasic.pinnolbe.dto.login.oauth2.UserDto;
import jpabasic.pinnolbe.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service

//DefaultOAuth2UserService -> user 정보를 가져오는 걸 포함하는 클래스
// CustomOAuth2User 생성 -> SecurityContext에 저장
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        OAuth2User oAuth2User=super.loadUser(userRequest);
        System.out.println(oAuth2User);

        // 추후 작성
        OAuth2Response oAuth2Response=new KakaoResponse(oAuth2User.getAttributes());

        //OAuth2User를 SecurityConfig에 등록해야 사용할 수 있음

        //리소스 서버에서 발급 받은 정보로 사용자를
        String username= oAuth2Response.getId();
        User existData=userRepository.findByUsername(username);

        if(existData==null){

            User user=new User();
            user.setUsername(username);
            user.setEmail(oAuth2Response.getEmail());
            user.setName(oAuth2Response.getName());
            user.setRole("ROLE_USER");

            userRepository.save(user);

            UserDto userDto=new UserDto();
            userDto.setUsername(username);
            userDto.setName(oAuth2Response.getName());
            userDto.setRole("ROLE_USER");

            userDto.setChildName(null);

            return new CustomOAuth2User(userDto);

        }
        else{

            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());

            userRepository.save(existData);

            UserDto userDto=new UserDto();
            userDto.setUsername(existData.getUsername());
            userDto.setName(oAuth2Response.getName());
            userDto.setRole(existData.getRole());

            userDto.setChildName(existData.getChildName());

            return new CustomOAuth2User(userDto);


        }











    }
}
