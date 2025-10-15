package org.example.unibooker.infrastructure.email;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {

    /**
     * HTML 이메일 발송
     *
     * @param to 수신자 이메일
     * @param subject 이메일 제목
     * @param htmlContent HTML 본문
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 매니저 계정 생성 이메일 발송 (편의 메서드)
     *
     * @param to 수신자 이메일 (매니저)
     * @param name 매니저 이름
     * @param companyName 회사명
     * @param tempPassword 임시 비밀번호
     */
    void sendManagerCreationEmail(String to, String name, String companyName, String tempPassword);
}