package org.example.unibooker.infrastructure.email;

import org.example.unibooker.domain.user.model.UserRole;

import java.time.LocalDateTime;

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

    /**
     * 관리자 승인 이메일 발송 (편의 메서드)
     *
     * @param to 수신자 이메일 (관리자)
     * @param name 관리자 이름
     * @param companyName 회사명
     * @param tempPassword 임시 비밀번호
     * @param serviceUrl 서비스 URL
     */
    void sendAdminApprovalEmail(String to, String name, String companyName, String tempPassword, String serviceUrl);

    /**
     * 비밀번호 찾기 이메일 발송
     *
     * @param to 수신자 이메일
     * @param name 사용자 이름
     * @param companyName 기업명
     * @param tempPassword 임시 비밀번호
     */
    void sendPasswordResetEmail(String to, String name, String companyName, String tempPassword);

    /**
     * 계정 삭제 완료 이메일 발송
     * - 72시간 미로그인으로 인한 자동 삭제 안내
     *
     * @param to 수신자 이메일 (당사자)
     * @param name 사용자 이름
     * @param role 사용자 역할 (ADMIN/MANAGER)
     */
    void sendAccountDeletionNotice(String to, String name, UserRole role);

    /**
     * 기업 가입 거절 이메일 발송
     *
     * @param to 수신자 이메일 (관리자)
     * @param name 관리자 이름
     * @param companyName 기업명
     * @param businessNumber 사업자등록번호
     * @param appliedDate 신청일
     * @param rejectionReason 거절 사유
     */
    void sendCompanyRejectionEmail(
            String to,
            String name,
            String companyName,
            String businessNumber,
            LocalDateTime appliedDate,
            String rejectionReason
    );
}