package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.ResourceDto;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.model.ResourceStatus;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final ResourceGroupRepository resourceGroupRepository;


    // -------------------- 리소스 등록 --------------------
    @Transactional
    public void register(ResourceDto.ResourceRegisterReq dto) {
        // TODO : 생성자, 수정자

        ResourceGroups group = resourceGroupRepository.findById(dto.getResourceGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        Resources resource = dto.toEntity(group);

        resourceRepository.save(resource);
    }
}
