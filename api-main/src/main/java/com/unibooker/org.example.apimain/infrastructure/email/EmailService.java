package com.unibooker.main.infrastructure.email;

import java.time.LocalDateTime;
import com.unibooker.common.enums.UserRole;

/**
 * 이메일 발송 서비스 인터페이스
 */
public interface EmailService {

    /**
     * HTML 이메일 발송
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 매니저 계정 생성 이메일 발송
     */
    void sendManagerCreationEmail(String to, String name, String companyName, String tempPassword);

    /**
     * 관리자 승인 이메일 발송
     */
    void sendAdminApprovalEmail(String to, String name, String companyName, String tempPassword, String serviceUrl);

    /**
     * 비밀번호 찾기 이메일 발송
     */
    void sendPasswordResetEmail(String to, String name, String companyName, String tempPassword);

    /**
     * 계정 삭제 완료 이메일 발송
     */
    void sendAccountDeletionNotice(String to, String name, UserRole role);

    /**
     * 기업 가입 거절 이메일 발송
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