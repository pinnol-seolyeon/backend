package jpabasic.pinnolbe.config;

import jpabasic.pinnolbe.converter.DateToLocalDateConverter;
import jpabasic.pinnolbe.converter.FromKstConverter;
import jpabasic.pinnolbe.converter.LocalDateToDateConverter;
import jpabasic.pinnolbe.converter.ToKstConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.List;

//MongoDB 저장 시 _class값(패키지 정보) 가 자동 추가되지 않도록 함.. 
@Configuration
public class MongoDBConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory dbFactory, MongoMappingContext mongoMappingContext, MongoCustomConversions conversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(dbFactory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        mappingConverter.setCustomConversions(conversions);
        mappingConverter.afterPropertiesSet();

        return mappingConverter;

    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                LocalDateToDateConverter.INSTANCE,
                DateToLocalDateConverter.INSTANCE,
                new ToKstConverter(),
                new FromKstConverter()
        ));
    }
}
