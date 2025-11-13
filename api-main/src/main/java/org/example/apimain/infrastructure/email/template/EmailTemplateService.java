package org.example.apimain.infrastructure.email.template;

import java.time.LocalDateTime;
import org.example.common.model.UserRole;

/**
 * 이메일 템플릿 렌더링 서비스 인터페이스
 */
public interface EmailTemplateService {

    String renderManagerCreationTemplate(String name, String companyName, String tempPassword);

    String renderAdminApprovalTemplate(String name, String companyName, String tempPassword, String serviceUrl);

    String renderPasswordResetTemplate(String name, String companyName, String tempPassword);

    String renderAccountDeletionTemplate(String name, UserRole role);

    String renderCompanyRejectionTemplate(String name, String companyName, String businessNumber,
                                          LocalDateTime appliedDate, String rejectionReason);
}