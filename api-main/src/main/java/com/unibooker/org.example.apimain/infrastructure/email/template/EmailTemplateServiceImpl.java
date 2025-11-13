package com.unibooker.main.infrastructure.email.template;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.unibooker.common.enums.UserRole;

/**
 * 이메일 템플릿 렌더링 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Value("${app.mail.login-url}")
    private String loginUrl;

    @Override
    public String renderManagerCreationTemplate(String name, String companyName, String tempPassword) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("companyName", companyName);
        variables.put("tempPassword", tempPassword);
        variables.put("loginUrl", loginUrl);
        return renderTemplate("email/ManagerCreation", variables);
    }

    @Override
    public String renderAdminApprovalTemplate(String name, String companyName, String tempPassword, String serviceUrl) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("companyName", companyName);
        variables.put("tempPassword", tempPassword);
        variables.put("loginUrl", loginUrl);
        variables.put("serviceUrl", serviceUrl);
        return renderTemplate("email/AdminApproval", variables);
    }

    @Override
    public String renderPasswordResetTemplate(String name, String companyName, String tempPassword) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("companyName", companyName);
        variables.put("tempPassword", tempPassword);
        return renderTemplate("email/password-reset-template", variables);
    }

    @Override
    public String renderAccountDeletionTemplate(String name, UserRole role) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("role", role.name());
        String roleKorean = role == UserRole.ADMIN ? "관리자" : "매니저";
        variables.put("roleKorean", roleKorean);
        return renderTemplate("email/AccountDeletion", variables);
    }

    @Override
    public String renderCompanyRejectionTemplate(String name, String companyName, String businessNumber,
                                                 LocalDateTime appliedDate, String rejectionReason) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("companyName", companyName);
        variables.put("businessNumber", businessNumber);
        variables.put("appliedDate", appliedDate);
        variables.put("rejectionReason", rejectionReason);
        variables.put("signupUrl", "https://unibooker.kro.kr/admin/signup");
        return renderTemplate("email/CompanyRejection", variables);
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}