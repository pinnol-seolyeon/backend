package jpabasic.pinnolbe.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jpabasic.pinnolbe.dto.login.jwt.JwtDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    public static final long EXPIRE_TIME=1000*60*5;

    @PostConstruct //초기화 시점에 실행
    void init(){
        this.secretKey=Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public JwtDto createToken(String email){
        String encryptedEmail=Aes256Util.encrypt(email);

        //토큰 생성 시 subject에 사용자 이메일 저장
        Claims claims= (Claims) Jwts.claims().setSubject(encryptedEmail);

        Date now=new Date();

        String accesssToken=Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return JwtDto.builder()
                .accessToken(accesssToken)
//                .refreshToken(refreshToken)
                .build();
    }

    public String getSubject(Claims claims){
        return Aes256Util.decrypt(claims.getSubject());
    }

    public Claims getClaims(String token){
        Claims claims;
        try{
            claims=Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }catch(ExpiredJwtException e){
            claims=e.getClaims();
        }catch(Exception e){
            throw new BadCredentialsException("유효한 토큰이 아닙니다.");
        }
        return claims;
    }
}
