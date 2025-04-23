package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.login.KaKaoTokenResponseDto;
import jpabasic.pinnolbe.dto.login.KakaoUserDto;
import jpabasic.pinnolbe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import java.util.Optional;

@Service
@Slf4j
public class KaKaoService {

    private final String clientId;

    @Autowired
    private UserRepository userRepository;


    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    public KaKaoService(@Value("${kakao.client_id}") String clientId) {
        this.clientId = clientId;
    }


    /// access token 받아오기
    public String getAccessTokenFromKakao(String code){
        log.info("⭐clientId:{}",clientId);
        log.info("⭐redirectURI:{}",redirectUri);
        KaKaoTokenResponseDto kaKaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST)
                .post()
                .uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8")
                .body(BodyInserters
                        .fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri",redirectUri)
                        .with("code", code)
                )
                .retrieve() /// 요청 전송+응답 수신 준비
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("🔴 4xx Error Response Body: {}", errorBody);
                            return Mono.error(new RuntimeException("Invalid Parameter: " + errorBody));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KaKaoTokenResponseDto.class)///응답 바디를 단일 객체로 변환 ///응답 바디를 이 클래스에 맞춰 자동 파싱
                .block();

        String accessToken=kaKaoTokenResponseDto.getAccessToken();
        return accessToken;
    }


    /// 카카오로부터 user 정보 받아오기
    public KakaoUserDto getUserInfoFromKakao(String accessToken){

        KakaoUserDto userInfo=WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri("/v2/user/me")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("🔴 4xx Error Response Body: {}", errorBody);
                            return Mono.error(new RuntimeException("Invalid Parameter: " + errorBody));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserDto.class)///응답 바디를 단일 객체로 변환 ///응답 바디를 이 클래스에 맞춰 자동 파싱
                .block();

                return userInfo;

    }


   /// 로그인 OR 회원가입 여부 결정
    public User ifIsMember(KakaoUserDto kakaoUserDto) {

        String username = kakaoUserDto.getProperties().getNickname();
        String email = kakaoUserDto.getKakaoAccount().getEmail();

        try {
            // 이메일로 사용자 조회
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                log.info("✏️기존 회원 로그인:{}", email);
                return user.get(); //기본 회원 반환
                //🤨 로그인 로직 추가
            } else {
                log.info("✖️신규 회원가입 진행:{}", email);
                User newUser = User.builder()
                        .email(email)
                        .username(username)
                        .build();
                userRepository.save(newUser);
                return newUser; //신규 회원 반환
            }
        } catch (Exception e) {
            log.error("✖️ 로그인 에러 발생:{}", e.getMessage());
            throw new RuntimeException("카카오 로그인 실패", e);
        }

    }


}
