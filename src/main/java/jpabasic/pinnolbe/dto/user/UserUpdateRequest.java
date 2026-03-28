package jpabasic.pinnolbe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 정보 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "유저 이름", example = "김민서")
        String userName,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String userPhoneNumber,
        @Schema(description = "보호자 성함")
        String parentsName,
        @Schema(description = "보호자 전화번호")
        String parentsPhoneNumber
) {}
