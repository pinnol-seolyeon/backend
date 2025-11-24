package jpabasic.pinnolbe.dto.reward;

import jpabasic.pinnolbe.domain.reward.Reward;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponseDto {
    private String id;
    private String userId;
    private String category;
    private Integer amount;
    private LocalDateTime createdAt;

    public static RewardResponseDto from(Reward reward){
        return RewardResponseDto.builder()
                .id(reward.getId())
                .userId(reward.getUserId())
                .category(reward.getCategory().getDescription().toString())
                .amount(reward.getCoin())
                .createdAt(reward.getCreatedAt())
                .build();

    }
}
