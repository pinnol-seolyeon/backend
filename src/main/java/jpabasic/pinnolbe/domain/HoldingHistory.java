package jpabasic.pinnolbe.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
class HoldingHistory {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public HoldingHistory(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void close(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
