package jpabasic.pinnolbe.jwt;


import io.jsonwebtoken.*;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        System.out.println("✅✅✅secret" + secret);
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }


    public String getUsername(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }
    }

    public String getRole(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        }
    }

    public Boolean isExpired(String token) {
        try {
            System.out.println("🖥️ isExpired 확인 시도");
            JwtParser parser = Jwts.parser().verifyWith(secretKey).build();
            Jws<Claims> claimsJws = parser.parseSignedClaims(token);
            Date exp = claimsJws.getPayload().getExpiration();
            System.out.println("🖥️ exp: " + exp);
            return exp.before(new Date());
        } catch (ExpiredJwtException e) {
            System.out.println("🖥️ expiredDate (catch): " + e.getClaims().getExpiration());
            return true;
        } catch(Exception e){
            return true;
        }
    }


    public String createJwt(String username, String role, Long expiredMs) {

        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }


}
