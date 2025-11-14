package org.example.common.model;

/**
 * 기업 상태
 */
public enum CompanyStatus {
    PENDING("승인 대기", "Pending"),
    ACTIVE("활성", "Active"),
    SUSPENDED("정지", "Suspended"),
    REJECTED("승인 거절", "Rejected");

    private final String koreanName;
    private final String englishName;

    CompanyStatus(String koreanName, String englishName) {
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