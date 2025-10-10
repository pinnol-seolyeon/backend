package jpabasic.pinnolbe.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {
    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("✅ [TIMEZONE 설정 완료] 서버 타임존 = " + TimeZone.getDefault().getID());
    }
}
