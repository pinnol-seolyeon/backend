package jpabasic.pinnolbe.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.redis.core.index.Indexed;
import jakarta.persistence.MappedSuperclass;


import java.time.LocalDateTime;

@Getter
@Document
public abstract class BaseEntity {

    @CreatedDate
    @Field("createdAt")
    @Indexed
    public LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    @Indexed
    private LocalDateTime updatedAt;
}
