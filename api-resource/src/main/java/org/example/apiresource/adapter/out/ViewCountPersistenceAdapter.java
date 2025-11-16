package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.ViewCountRepository;
import org.example.apiresource.usecase.port.out.ViewCountPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ViewCountPersistenceAdapter implements ViewCountPersistencePort {

    private final ViewCountRepository viewCountRepository;

    @Override
    @Transactional
    public long sumViewsByResourceGroupIdAndDate(Long resourceGroupId, LocalDateTime start, LocalDateTime end) {
        return viewCountRepository.sumViewsByResourceGroupIdAndDate(resourceGroupId, start, end);
    }


    @Override
    @Transactional
    public List<Object[]> getTodayHourlyViews(Long resourceGroupId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return viewCountRepository.getTodayHourlyViews(resourceGroupId, startOfDay, endOfDay);
    }


    // 조회수 테이블에 저장된 값을 이용하고 싶다면 viewCountPersistencePort 사용
    // 리소스 그룹에 저장된 viewCount를 사용하고 싶다면 resourceGroupPersistencePort 사용

    // ResourceTimeSlotPersistenceAdapter에 구현 안 된 부분있음.
}
