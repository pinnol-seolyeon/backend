package jpabasic.pinnolbe.dto.login.jwt;

import lombok.Builder;

@Builder
public record JwtDto (String accessToken,String refreshToken){}
