package org.example.unibooker.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.infrastructure.email.EmailService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 미활성 계정 삭제 처리
 * - 이메일 발송 후 ItemWriter로 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountProcessor implements ItemProcessor<Users, Users> {

    private final EmailService emailService;

    @Override
    public Users process(Users account) throws Exception {
        // 1. 삭제 전 로그 기록
        log.warn("[BATCH_HARD_DELETE] id={}, email={}, role={}, companyId={}, name={}, createdAt={}",
                account.getId(),
                account.getEmail(),
                account.getRole(),
                account.getCompany().getId(),
                account.getName(),
                account.getCreatedAt());

        // 2. 본인에게 삭제 완료 이메일 발송
        try {
            emailService.sendAccountDeletionNotice(
                    account.getEmail(),
                    account.getName(),
                    account.getRole()
            );
            log.info("[InactiveAccountProcessor] Deletion notice sent to: {}", account.getEmail());
        } catch (Exception e) {
            log.error("[InactiveAccountProcessor] Failed to send email to {}: {}",
                    account.getEmail(), e.getMessage());
        }

        return account;
    }
}