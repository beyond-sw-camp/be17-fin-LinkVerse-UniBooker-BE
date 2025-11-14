package org.example.common.model;

/**
 * 사용자 상태
 */
public enum UserStatus {
    ACTIVE("활성", "Active"),
    INACTIVE("비활성", "Inactive"),
    SUSPENDED("정지", "Suspended"),
    DELETED("삭제", "Deleted"),
    PENDING("승인 대기", "Pending");

    private final String koreanName;
    private final String englishName;

    UserStatus(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }
}