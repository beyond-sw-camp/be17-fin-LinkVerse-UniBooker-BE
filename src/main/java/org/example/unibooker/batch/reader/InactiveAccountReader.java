package org.example.unibooker.batch.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * 미활성 계정 조회 Reader
 * - ADMIN/MANAGER 중 첫 로그인 미완료 계정
 * - 설정된 시간 이상 미활성 계정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountReader implements ItemReader<Users> {

    private final UserRepository userRepository;

    @Value("${app.batch.inactive-minutes:4320}")
    private int inactiveMinutes;

    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    private Iterator<Users> iterator;

    @Override
    public Users read() throws Exception {
        if (iterator == null) {
            iterator = fetchInactiveAccounts().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 미활성 계정 조회
     */
    private List<Users> fetchInactiveAccounts() {
        if (!enabled) {
            log.debug("[InactiveAccountReader] Batch is disabled");
            return List.of();
        }

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(inactiveMinutes);
        log.info("[InactiveAccountReader] Cutoff time: {}", cutoffTime);

        List<Users> inactiveAccounts = userRepository
                .findByRoleInAndStatusAndIsFirstLoginAndCreatedAtBefore(
                        List.of(UserRole.ADMIN, UserRole.MANAGER),
                        UserStatus.ACTIVE,
                        true,
                        cutoffTime
                );

        log.info("[InactiveAccountReader] Found {} inactive accounts", inactiveAccounts.size());
        return inactiveAccounts;
    }
}