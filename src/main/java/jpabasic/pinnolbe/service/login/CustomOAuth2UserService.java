package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.dto.login.oauth2.KakaoResponse;
import jpabasic.pinnolbe.dto.login.oauth2.OAuth2Response;
import jpabasic.pinnolbe.dto.login.oauth2.UserDto;
import jpabasic.pinnolbe.dto.reward.RewardDto;
import jpabasic.pinnolbe.repository.RewardRepository;
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
    private final RewardRepository rewardRepository;

    public CustomOAuth2UserService(UserRepository userRepository,RewardRepository rewardRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
    }

    @Override
    //OAuth2UserRequest : 리소스서버에서 제공하는 유저 정보
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        OAuth2User oAuth2User=super.loadUser(userRequest); //생성자를 부름 -> 유저 정보 가져옴
        System.out.println("✏️✏️"+oAuth2User);

        // 추후 작성
        OAuth2Response oAuth2Response=new KakaoResponse(oAuth2User.getAttributes());
        System.out.println("✅OAUth2Response"+oAuth2Response);

        //OAuth2User를 SecurityConfig에 등록해야 사용할 수 있음

        //리소스 서버에서 발급 받은 정보로 사용자 구분
        String username= oAuth2Response.getId();
        System.out.println("✅"+username);

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


            Reward reward=new Reward();
            reward.setUserId(user.getId());
            reward.setCoin(0L);
            rewardRepository.save(reward);


//            RewardDto rewardDto=new RewardDto();
//            rewardDto.setUserId(user.getId());

            System.out.println("✅ 새로운 유저"+userDto);
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

            System.out.println("✅유저"+userDto);
            return new CustomOAuth2User(userDto);


        }











    }
}
