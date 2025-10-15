package org.example.unibooker.domain.company.model;

public enum CompanyStatus {
    PENDING("승인 대기", "Pending"),
    APPROVED("승인 완료", "Approved"),
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

    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return koreanName;
        }
        return englishName;
    }
}
