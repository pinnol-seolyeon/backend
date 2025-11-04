package jpabasic.pinnolbe.dto.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderNameType {
    MONTH("한 달 결제"),
    YEAR("일년 결제");

    private final String name;

   OrderNameType(String name) {
        this.name = name;
    }

    @JsonValue
    public static String getName() {
        return name;
    }

    @JsonCreator
    public static OrderNameType from(String value) {
        //영문/한글 둘다 인식
        for (OrderNameType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.name.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
