package org.example.apiapp.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiapp.infrastructure.email.template.EmailTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.example.common.model.UserRole;

/**
 * 이메일 발송 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ 이메일 발송 성공 - 수신자: {}, 제목: {}", to, subject);

        } catch (MessagingException e) {
            log.error("❌ 이메일 메시지 생성 실패 - 수신자: {}", to, e);
            throw new RuntimeException("이메일 메시지 생성에 실패했습니다", e);
        } catch (MailException e) {
            log.error("❌ 이메일 발송 실패 - 수신자: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    @Override
    public void sendManagerCreationEmail(String to, String name, String companyName, String tempPassword) {
        String htmlContent = templateService.renderManagerCreationTemplate(name, companyName, tempPassword);
        String subject = "[UniBooker] 매니저 계정이 생성되었습니다";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendAdminApprovalEmail(String to, String name, String companyName, String tempPassword, String serviceUrl) {
        String htmlContent = templateService.renderAdminApprovalTemplate(name, companyName, tempPassword, serviceUrl);
        String subject = "[UniBooker] 기업 가입이 승인되었습니다";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendPasswordResetEmail(String to, String name, String companyName, String tempPassword) {
        String htmlContent = templateService.renderPasswordResetTemplate(name, companyName, tempPassword);
        String subject = "[UniBooker] 임시 비밀번호가 발급되었습니다";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendAccountDeletionNotice(String to, String name, UserRole role) {
        String htmlContent = templateService.renderAccountDeletionTemplate(name, role);
        String subject = "[UniBooker] 계정이 삭제되었습니다";
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendCompanyRejectionEmail(String to, String name, String companyName, String businessNumber,
                                          LocalDateTime appliedDate, String rejectionReason) {
        String htmlContent = templateService.renderCompanyRejectionTemplate(
                name, companyName, businessNumber, appliedDate, rejectionReason);
        String subject = "[UniBooker] 기업 가입 신청이 거절되었습니다";
        sendHtmlEmail(to, subject, htmlContent);
    }
}