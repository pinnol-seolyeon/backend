package jpabasic.pinnolbe.global.logging.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogContext {
    public String userId;
    public String httpMethod;
    public String endpoint;
    public long startTime;
    public List<String> methodDetails = new ArrayList<>(); // 메서드별 실행시간 저장

    public LogContext(String remoteUser, String method) {
        this.userId=remoteUser;
        this.httpMethod=method;
    }
}