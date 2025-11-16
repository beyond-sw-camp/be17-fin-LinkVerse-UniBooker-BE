package org.example.apiresource.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.adapter.out.repository.ResourceRepository;
import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceItemWriter implements ItemWriter<Resources> {

    private final ResourceRepository resourceRepository;

    @Override
    public void write(Chunk<? extends Resources> items) {
        if (items.isEmpty()) {
            log.info("업데이트할 리소스가 없습니다.");
            return;
        }

        log.info("=== [리소스 상태 업데이트 시작] ===");
        for (Resources resource : items) {
            log.info("리소스 ID: {}, 상태: {}", resource.getId(), resource.getStatus());
        }

        resourceRepository.saveAll(items);

        log.info("=== [리소스 상태 업데이트 완료] ===");
    }
}
