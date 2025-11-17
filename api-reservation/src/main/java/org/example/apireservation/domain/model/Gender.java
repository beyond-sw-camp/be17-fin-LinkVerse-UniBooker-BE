package org.example.apireservation.domain.model;

import lombok.Getter;

/**
 * 성별 (ERD users.gender)
 * - MALE: 남성
 * - FEMALE: 여성
 * - UNDEFINED: 미정의
 */
@Getter
public enum Gender {
    MALE("남성", "Male"),
    FEMALE("여성", "Female"),
    UNDEFINED("미정의", "Undefined");

    private final String koreanName;
    private final String englishName;

    Gender(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    /**
     * 언어에 따른 성별명 반환
     */
    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return koreanName;
        }
        return englishName;
    }
}