package jpabasic.pinnolbe.service.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.user.PhoneRequestDto;
import jpabasic.pinnolbe.dto.user.UserInfoDto;
import jpabasic.pinnolbe.dto.login.oauth2.CustomOAuth2User;
import jpabasic.pinnolbe.jwt.JwtUtil;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import jpabasic.pinnolbe.repository.RewardRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    //로그인 된 상태에서 유저 정보 가져오기
    @Transactional
    public User getUserInfo(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User oAuth2User = (CustomOAuth2User) auth.getPrincipal();

        String username=oAuth2User.getUsername(); //인증 정보 꺼냄
        User user=userRepository.findByUsername(username); //DB에서 최신 정보 조회

        return user;
    }

    //첫 로그인 시 자녀 정보 입력하기
    @Transactional
    public void inputUserInfo(User user, PhoneRequestDto dto){

        if(user==null){
            throw new IllegalArgumentException("유저 정보 ✖️");
        }

        if(dto==null){
            throw new IllegalArgumentException("자녀 정보 ✖️");
        }

        try {
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setAgreement(dto.getAgreement());
            userRepository.save(user);
        }catch(Exception e){
            throw new RuntimeException("유저 자녀 정보를 저장하는 중 오류 발생");
        }
    }


    //유저 정보 받아오기
    @Transactional
    public UserInfoDto getUserInfoDto(User user){

        return new UserInfoDto(
                user.getUsername(),
                user.getName(),
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
        RefreshToken token=new RefreshToken(refreshToken,username);
        refreshTokenRepository.save(token);

        log.info("✅ user RefreshToken 생성 후 저장 완료");
    }


    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response,String refreshToken){
        //Refresh Token 유효 시 DB에서 제거
        if(refreshToken!=null){
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        ResponseCookie accessCookieDel=deleteAccessToken(request);
        ResponseCookie refreshCookieDel=deleteRefreshToken(request);

        //응답 헤더에 삭제 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE,accessCookieDel.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,refreshCookieDel.toString());

    }

    /**
     * access token 삭제(로그아웃)
     * @param request
     * @return
     */
    private ResponseCookie deleteAccessToken(HttpServletRequest request){
        ResponseCookie accessCookie=ResponseCookie.from("Authorization","")
                .path("/")
                .maxAge(0) //즉시 만료
                .httpOnly(true)
                /*localhost에서 작동하도록 주석처리해놓음.배포시에는 주석 풀것 ‼️*/
//                .secure(true)
                .sameSite("Strict")
                .build();
        return accessCookie;
    }

    /**
     * refresh token 삭제(로그아웃)
     * @param request
     * @return
     */
    private ResponseCookie deleteRefreshToken(HttpServletRequest request){
        ResponseCookie accessCookie=ResponseCookie.from("RefreshToken","")
                .path("/")
                .maxAge(0) //즉시 만료
                .httpOnly(true)
                /*localhost에서 작동하도록 주석처리해놓음.배포시에는 주석 풀것 ‼️*/
//                .secure(true)
                .sameSite("Strict")
                .build();
        return accessCookie;
    }
}
