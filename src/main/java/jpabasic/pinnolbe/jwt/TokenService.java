package jpabasic.pinnolbe.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jpabasic.pinnolbe.domain.RefreshToken;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_COOKIE));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.NO_COOKIE);
        }

        String newAccessToken = jwtUtil.createJwt(username, role, 5 * 60 * 1000L);
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
