package jpabasic.pinnolbe.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@ReadingConverter
public class FromKstConverter implements Converter<Date, LocalDateTime> {
    @Override
    public LocalDateTime convert(Date source) {
        // Date (UTC) → KST(LocalDateTime)
        return source.toInstant()
                .atZone(ZoneId.of("Asia/Seoul"))  // 올바른 ZoneId 사용
                .toLocalDateTime();               // ZonedDateTime → LocalDateTime
    }
}
