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
import org.springframework.web.bind.annotation.PathVariable;

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
        dto.validate();

        ResourceGroups group = resourceGroupRepository.findById(dto.getResourceGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        Resources resource = dto.toEntity(group);

        resource.setTimeInterval(dto.getTimeInterval());

        resourceRepository.save(resource);
    }


    // -------------------- 리소스 상세 조회 (수정용) --------------------
    public ResourceDto.ResourceUpdateRes getResourceDetailForUpdate(Long resourceGroupId) {
        Resources resource = resourceRepository.findByResourceGroupId(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스 그룹입니다."));

        return ResourceDto.ResourceUpdateRes.fromEntity(resource);
    }


    // -------------------- 리소스 상세 조회 (목록 조회용) --------------------
    public ResourceDto.ResourceListInfo getResourceById(Long resourceId) {
        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return ResourceDto.ResourceListInfo.fromEntity(resource);
    }


    // -------------------- 리소스 수정 --------------------
    @Transactional
    public void update(Long resourceId, ResourceDto.ResourceUpdateReq dto) {
        dto.validate();

        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스가 존재하지 않습니다."));

        resource.update(dto);
    }
}
