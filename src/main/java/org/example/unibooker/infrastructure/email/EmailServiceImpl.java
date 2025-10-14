package org.example.unibooker.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.infrastructure.email.template.EmailTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스 구현체
 * JavaMailSender를 사용하여 실제 이메일을 발송합니다.
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
            helper.setText(htmlContent, true); // true = HTML 형식

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
    public void sendAdminApprovalEmail(String to, String name, String companyName, String tempPassword) {
        String htmlContent = templateService.renderAdminApprovalTemplate(name, companyName, tempPassword);
        String subject = "[UniBooker] 기업 가입이 승인되었습니다";

        sendHtmlEmail(to, subject, htmlContent);
    }
}