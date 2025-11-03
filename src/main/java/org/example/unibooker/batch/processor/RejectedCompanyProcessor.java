package org.example.unibooker.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 거절된 기업 삭제 처리
 * - 연결된 사용자도 함께 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyProcessor implements ItemProcessor<Companies, Companies> {

    private final UserRepository userRepository;

    @Override
    public Companies process(Companies company) throws Exception {
        // 1. 기업 삭제 전 로그 기록
        log.warn("[BATCH_HARD_DELETE] Company id={}, companyName={}, slug={}, createdAt={}",
                company.getId(),
                company.getCompanyName(),
                company.getCompanySlug(),
                company.getCreatedAt());

        // 2. 연결된 사용자 조회 (삭제 시 함께 제거)
        List<Users> users = userRepository.findByCompany_Id(company.getId());

        if (!users.isEmpty()) {
            log.info("[RejectedCompanyProcessor] Found {} users for company id={}",
                    users.size(), company.getId());
            users.forEach(user -> log.warn("[BATCH_HARD_DELETE] User id={}, email={}, role={}",
                    user.getId(), user.getEmail(), user.getRole()));
        }

        return company;
    }
}