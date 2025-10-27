package org.example.unibooker.infrastructure.email.template;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.user.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * 이메일 템플릿 렌더링 서비스 구현체
 * Thymeleaf를 사용하여 HTML 템플릿을 렌더링합니다.
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

    /**
     * Thymeleaf 템플릿 렌더링 공통 메서드
     *
     * @param templateName 템플릿 이름 (resources/templates/ 기준 경로)
     * @param variables 템플릿에 전달할 변수 맵
     * @return 렌더링된 HTML 문자열
     */
    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process(templateName, context);
    }

    @Override
    public String renderAccountDeletionTemplate(String name, UserRole role) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("role", role.name());  // "ADMIN" 또는 "MANAGER"

        String roleKorean = role == UserRole.ADMIN ? "관리자" : "매니저";
        variables.put("roleKorean", roleKorean);

        return renderTemplate("email/AccountDeletion", variables);
    }
}