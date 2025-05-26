package jpabasic.pinnolbe.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.sql.Timestamp;

@Component
@WritingConverter
public class ToKstConverter implements Converter<LocalDateTime, Date> {

    @Override
    public java.util.Date convert(LocalDateTime source) {
        //KST 보정:UTC+9
        return Timestamp.valueOf(source.plusHours(9));
    }
}
