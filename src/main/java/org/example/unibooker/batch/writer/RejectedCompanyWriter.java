package org.example.unibooker.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 거절된 기업 및 연결 사용자 하드 삭제 Writer
 * - 기업 및 연결된 사용자 일괄 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyWriter implements ItemWriter<Companies> {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    public void write(Chunk<? extends Companies> chunk) throws Exception {
        if (chunk.isEmpty()) {
            log.info("[RejectedCompanyWriter] No companies to delete");
            return;
        }

        long deleteCount = 0;
        long userDeleteCount = 0;

        for (Companies company : chunk.getItems()) {
            try {
                // 1. 기업 연결 모든 User 삭제
                List<Users> users = userRepository.findByCompany_Id(company.getId());
                if (!users.isEmpty()) {
                    userRepository.deleteAll(users);
                    userDeleteCount += users.size();
                    log.info("[RejectedCompanyWriter] Deleted {} users for company id={}",
                            users.size(), company.getId());
                }

                // 2. Company 하드 삭제
                companyRepository.delete(company);
                deleteCount++;

                log.info("[RejectedCompanyWriter] Deleted company id={}, name={}",
                        company.getId(), company.getCompanyName());

            } catch (Exception e) {
                log.error("[RejectedCompanyWriter] Failed to delete company id={}: {}",
                        company.getId(), e.getMessage(), e);
            }
        }

        log.info("[RejectedCompanyWriter] Batch completed - Companies: {}, Users: {}",
                deleteCount, userDeleteCount);
    }
}