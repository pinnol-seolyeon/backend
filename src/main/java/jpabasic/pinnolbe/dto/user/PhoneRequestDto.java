package jpabasic.pinnolbe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class PhoneRequestDto {

    @Schema(description="보호자 전화번호 (- 포함)", example="010-1234-1234")
    @Pattern(
            regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
            message="전화번호 형식이 올바르지 않습니다."
    )
    private String phoneNumber;

    @Schema(description="부모님 이름",example="김부모")
    private String name;

    @Schema(description = "개인정보 수집/이용 동의 여부")
    private Boolean agreement;
}
