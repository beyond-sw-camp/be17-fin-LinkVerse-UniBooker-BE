package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.ResourceTimeSlots;
import org.example.unibooker.domain.resource.model.TimeSlotDto;
import org.example.unibooker.domain.resource.repository.ResourceTimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final ResourceTimeSlotRepository resourceTimeSlotRepository;


    // -------------------- 리소스 ID로 isActive=true인 시간 슬롯 조회 --------------------
    @Transactional
    public List<TimeSlotDto.TimeSlotResponse> getTimeSlots(Long resourceId) {
        List<ResourceTimeSlots> slots = resourceTimeSlotRepository.findByResources_IdAndIsActiveTrue(resourceId);

        return slots.stream()
                .map(TimeSlotDto.TimeSlotResponse::from)
                .collect(Collectors.toList());
    }
}
