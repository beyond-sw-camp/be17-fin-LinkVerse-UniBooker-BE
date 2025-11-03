package org.example.unibooker.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 미활성 계정 하드 삭제 Writer
 * - 일괄 삭제 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountWriter implements ItemWriter<Users> {

    private final UserRepository userRepository;

    @Override
    public void write(Chunk<? extends Users> chunk) throws Exception {
        if (chunk.isEmpty()) {
            log.info("[InactiveAccountWriter] No accounts to delete");
            return;
        }

        long deleteCount = chunk.getItems().size();
        log.info("[InactiveAccountWriter] Deleting {} inactive accounts", deleteCount);

        userRepository.deleteAll(chunk.getItems());

        log.info("[InactiveAccountWriter] Successfully deleted {} accounts", deleteCount);
    }
}