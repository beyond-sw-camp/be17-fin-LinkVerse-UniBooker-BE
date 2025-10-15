package org.example.unibooker.domain.user.model;

public enum UserRole {
    USER("일반 사용자", "User"),
    MANAGER("매니저", "Manager"),
    ADMIN("관리자", "Admin"),
    SUPER("플렛폼 관리자", "Super Admin");

    private final String koreanName;
    private final String englishName;

    UserRole(String koreanName, String englishName) {
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