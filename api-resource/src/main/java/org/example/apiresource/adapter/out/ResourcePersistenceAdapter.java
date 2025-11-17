package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.ResourceRepository;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.entity.Resources;
import org.example.apiresource.domain.service.ResourceService;
import org.example.apiresource.usecase.port.in.ResourceWebPort;
import org.example.apiresource.usecase.port.out.ResourcePersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourcePersistenceAdapter implements ResourcePersistencePort {

    private final ResourceRepository resourceRepository;

    // 리소스 생성
    @Override
    @Transactional
    public void createResource(Resources resource) {
        resourceRepository.save(resource);
    }


    // 리소스 목록 조회
    @Override
    @Transactional
    public List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long serviceGroupId) {
        return resourceRepository.findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(serviceGroupId);
    }


    // 리소스 단건 조회
    @Override
    @Transactional
    public Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId) {
        return resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId);
    }

    @Override
    @Transactional
    public void saveResource(Resources resource) {
        resourceRepository.save(resource);
    }

    @Override
    @Transactional
    public void save(Resources resource) {
        resourceRepository.save(resource);
    }

    @Override
    public Optional<Resources> findById(Long resourceId) {
        return resourceRepository.findById(resourceId);
    }


    // 리소스 존재 여부 확인
    @Override
    @Transactional
    public boolean existsById(Long resourceId) {
        return resourceRepository.existsById(resourceId);
    }


    // 리소스 상세 조회 (활성화 & 미삭제 상태 & 비관적 락)
    @Override
    @Transactional
    public Optional<Resources> findByIdForUpdate(Long resourceId) {
        return resourceRepository.findByIdForUpdate(resourceId);
    }


    @Override
    @Transactional
    public int countAllByResourceGroup_Id(Long id) {
        return resourceRepository.countAllByResourceGroup_Id(id);
    }


    @Override
    @Transactional
    public int countAllByResourceGroup_CompanyId(Long companyId) {
        return resourceRepository.countAllByResourceGroup_CompanyId(companyId);
    }
}