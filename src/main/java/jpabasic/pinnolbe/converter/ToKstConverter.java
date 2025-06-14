package jpabasic.pinnolbe.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;

@Component
@WritingConverter
public class ToKstConverter implements Converter<LocalDateTime, Date> {
    @Override
    public Date convert(LocalDateTime source) {
        // ❌ 보정 없이 UTC로 저장되도록 해야 함
        return Date.from(source.atZone(ZoneId.of("Asia/Seoul")).toInstant());
    }
}
