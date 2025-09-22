package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.User.UserInfoDto;
import jpabasic.pinnolbe.dto.login.ChildInfoDto;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import jpabasic.pinnolbe.repository.RewardRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, RewardRepository rewardRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    //로그인 된 상태에서 유저 정보 가져오기
    @Transactional
    public User getUserInfo(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User oAuth2User = (CustomOAuth2User) auth.getPrincipal();

        String username=oAuth2User.getUsername(); //인증 정보 꺼냄
        User user=userRepository.findByUsername(username); //DB에서 최신 정보 조회

        System.out.println("🔍 Principal 클래스: " + auth.getPrincipal().getClass().getName());


        return user;
    }

    //첫 로그인 시 자녀 정보 입력하기
    @Transactional
    public void inputUserInfo(User user, ChildInfoDto dto){

        if(user==null){
            throw new IllegalArgumentException("유저 정보 ✖️");
        }

        if(dto==null){
            throw new IllegalArgumentException("자녀 정보 ✖️");
        }

        try {
            user.setChildAge(dto.getChildAge());
            user.setChildName(dto.getChildName());
            user.setPhoneNumber(dto.getPhoneNumber());

            userRepository.save(user);
        }catch(Exception e){
            throw new RuntimeException("유저 자녀 정보를 저장하는 중 오류 발생");
        }
    }


    //유저 정보 받아오기
    @Transactional
    public UserInfoDto getUserInfoDto(User user){
        String userId=user.getId();
//        Reward reward=rewardRepository.findByUserId(userId);
//
//        Long coin=reward.getCoin();

        return new UserInfoDto(
                user.getUsername(),
                user.getChildName(),
                user.getReward()

        );
    }


    //refresh Token 관련
    @Transactional
    public void deleteExpiredRefreshToken(RefreshToken refreshToken){
        refreshTokenRepository.delete(refreshToken);
        log.info("✅ 만료된 refresh token 삭제");
    }

    @Transactional
    public void saveNewRefreshToken(String username,String refreshToken){
        User user=userRepository.findByUsername(username);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        RefreshToken token=new RefreshToken(refreshToken,username);
        refreshTokenRepository.save(token);

        log.info("✅ user RefreshToken 생성 후 저장 완료");
    }

    @Transactional
    public void saveExistingRefreshToken(String username,String refreshToken){
        User user=userRepository.findByUsername(username);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        log.info("✅ 기존 user RefreshToken 저장 완료");
    }
}
