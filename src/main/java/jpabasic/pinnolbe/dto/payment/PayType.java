package jpabasic.pinnolbe.dto.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PayType {
    CARD("카드"),
    EASY_PAY("토스 간편 결제");

    private final String name;

    PayType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static PayType from(String value) {
        //영문/한글 둘다 인식
        for (PayType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.name.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value);
    }
}
