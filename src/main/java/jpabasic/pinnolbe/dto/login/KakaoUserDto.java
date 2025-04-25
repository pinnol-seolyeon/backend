package jpabasic.pinnolbe.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserDto {

    private Long id;

    @JsonProperty("connected_at")
    private String connectedAt;

    private Properties properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    public static class Properties {
        private String nickname;
    }

    @Getter
    public static class KakaoAccount {
        private String email;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;

        @JsonProperty("has_email")
        private Boolean hasEmail;

        @JsonProperty("profile_nickname_needs_agreement")
        private Boolean profileNicknameNeedsAgreement;

        @JsonProperty("email_needs_agreement")
        private Boolean emailNeedsAgreement;

        @JsonProperty("is_email_valid")
        private Boolean isEmailValid;
    }
}
