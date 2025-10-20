package org.example.unibooker.domain.resource.model;

import lombok.Getter;

@Getter
public enum TimeIntervalType {
    THIRTY(30),
    SIXTY(60);

    private final int minutes;

    TimeIntervalType(int minutes) {
        this.minutes = minutes;
    }

    // 숫자로 enum 찾기
    public static TimeIntervalType fromMinutes(int minutes) {
        for (TimeIntervalType type : values()) {
            if (type.getMinutes() == minutes) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid minutes: " + minutes);
    }
}
