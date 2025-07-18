package jpabasic.pinnolbe.dto.login.oauth2;

import java.util.Map;

public class KakaoResponse implements OAuth2Response{

    private final Map<String,Object> attribute;

    public KakaoResponse(Map<String,Object> attribute) {

        this.attribute=attribute;
    }


    @Override
    public String getId(){
        return attribute.get("id").toString();
    }


    @Override
    public String getEmail() {
        Map<String,Object> kakaoAccount=(Map<String,Object>)attribute.get("kakao_account");
        return kakaoAccount.get("email").toString();
    }

//    @Override
//    public String getPhoneNumber(){
//
//    }

    @Override
    public String getName() {
        Map<String,Object> properties=(Map<String,Object>)attribute.get("properties");
        return properties.get("nickname").toString();
    }
}
