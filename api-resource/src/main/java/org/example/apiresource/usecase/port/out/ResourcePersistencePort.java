package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ResourcePersistencePort {

    // 리소스 생성
    void createResource(Resources resource);

    List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long serviceGroupId);

    Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId);

    void saveResource(Resources resource);

    void save(Resources resource);

    Optional<Resources> findById(Long resourceId);

    // 리소스 단건 조회 (수정용 - 삭제되지 않은 리소스)
    Optional<Resources> findByIdAndDeletedAtIsNull(Long resourceId);

    // 리소스 존재 여부 확인
    boolean existsById(Long resourceId);

    // 리소스 상세 조회 (활성화 & 미삭제 상태 & 비관적 락)
    Optional<Resources> findByIdForUpdate(Long resourceId);

    int countAllByResourceGroup_Id(Long id);

    int countAllByResourceGroup_CompanyId(Long companyId);
}