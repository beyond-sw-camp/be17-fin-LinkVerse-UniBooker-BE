package org.example.unibooker.infrastructure.email.template;

/**
 * 이메일 템플릿 렌더링 서비스 인터페이스
 */
public interface EmailTemplateService {

    /**
     * 매니저 계정 생성 이메일 템플릿 렌더링
     *
     * @param name 매니저 이름
     * @param companyName 회사명
     * @param tempPassword 임시 비밀번호
     * @return 렌더링된 HTML 문자열
     */
    String renderManagerCreationTemplate(String name, String companyName, String tempPassword);

    /**
     * 관리자 승인 이메일 템플릿 렌더링
     *
     * @param name 관리자 이름
     * @param companyName 회사명
     * @param tempPassword 임시 비밀번호
     * @param serviceUrl 서비스 URL
     * @return 렌더링된 HTML 문자열
     */
    String renderAdminApprovalTemplate(String name, String companyName, String tempPassword, String serviceUrl);
}