package jpabasic.pinnolbe.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@ReadingConverter
public enum DateToLocalDateConverter implements Converter<Date, LocalDate> {
    INSTANCE;
    @Override
    public LocalDate convert(Date source) {
        // 저장된 UTC Instant를 KST 날짜로 변환
        return source.toInstant()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }
}
