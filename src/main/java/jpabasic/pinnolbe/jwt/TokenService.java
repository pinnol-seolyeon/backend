package jpabasic.pinnolbe.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional
    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {

        String username = jwtUtil.getUsername(refreshToken);
        System.out.println("😎username: " + username);
        String role = jwtUtil.getRole(refreshToken);
        System.out.println("😎role: " + role);

        //오류 발생
        RefreshToken savedToken = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_COOKIE));
        String token=savedToken.getToken(); //기존에 저장되어 있던 refresh token
        if (!savedToken.getToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.NO_COOKIE);
        }

        String newAccessToken = jwtUtil.createJwt(username, role, 1 * 60 * 1000L);
        response.addCookie(createCookie("Authorization", newAccessToken, 5 * 60));
        return newAccessToken;
    }


    private Cookie createCookie(String key, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        return cookie;
    }
}
