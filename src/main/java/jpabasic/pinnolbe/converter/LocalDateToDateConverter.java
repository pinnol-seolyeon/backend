package jpabasic.pinnolbe.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

@WritingConverter
public enum LocalDateToDateConverter implements Converter<LocalDate, Date> {
    INSTANCE;
    @Override
    public Date convert(LocalDate source) {
        // UTC 자정 기준으로 Instant 생성 → 저장
        return Date.from(source.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
