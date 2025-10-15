package org.example.unibooker.domain.user.model;

public enum UserStatus {
    ACTIVE("활성", "Active"),
    INACTIVE("비활성", "Inactive"),
    SUSPENDED("정지", "Suspended"),
    DELETED("삭제", "Deleted");

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

    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return koreanName;
        }
        return englishName;
    }
}