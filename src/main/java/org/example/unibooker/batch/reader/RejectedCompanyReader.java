package org.example.unibooker.batch.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * 거절된 기업 조회 Reader
 * - REJECTED 상태의 기업
 * - 생성 후 설정된 일수 경과
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyReader implements ItemReader<Companies> {

    private final CompanyRepository companyRepository;

    @Value("${app.batch.company-retention-days:7}")
    private int retentionDays;

    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    private Iterator<Companies> iterator;

    @Override
    public Companies read() throws Exception {
        if (iterator == null) {
            iterator = fetchRejectedCompanies().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 거절된 기업 조회
     */
    private List<Companies> fetchRejectedCompanies() {
        if (!enabled) {
            log.debug("[RejectedCompanyReader] Batch is disabled");
            return List.of();
        }

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        log.info("[RejectedCompanyReader] Cutoff time: {} (retention days: {})",
                cutoffTime, retentionDays);

        List<Companies> rejectedCompanies = companyRepository
                .findByStatusAndCreatedAtBefore(CompanyStatus.REJECTED, cutoffTime);

        log.info("[RejectedCompanyReader] Found {} rejected companies",
                rejectedCompanies.size());
        return rejectedCompanies;
    }
}