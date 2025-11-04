package jpabasic.pinnolbe.dto.payment;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PayType {
    CARD("카드");

    private final String name;

    PayType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
