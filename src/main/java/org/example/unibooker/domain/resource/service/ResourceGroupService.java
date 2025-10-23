package org.example.unibooker.domain.resource.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.resource.model.CustomFieldDefinitions;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CustomFieldDefinitionRepository customFieldRepository;


    // -------------------- 관리자, 매니저 권한을 가졌는지 확인하는 함수 --------------------
    private void checkAdminOrManager(Users user) {
        if (user == null || !(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER)) {
            throw new IllegalArgumentException("권한이 없는 사용자입니다.");
        }
    }


    // -------------------- 리소스 그룹 등록 --------------------
    @Transactional
    public void register(ResourceGroupDto.ResourceGroupRegisterReq dto, Long userId, Long companyId) {

        // 기업 엔티티 조회
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기업 ID입니다."));

        // 사용자 엔티티 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다."));

        checkAdminOrManager(user);

        // 서비스 그룹 생성 후
        ResourceGroups group = dto.toEntity(user, company);
        resourceGroupRepository.save(group);

        // 커스텀 필드 생성
        if (dto.getCustomFields() != null && !dto.getCustomFields().isEmpty()) {
            List<CustomFieldDefinitions> fields = dto.getCustomFields().stream()
                    .map(fieldDto -> {
                        CustomFieldDefinitions entity = fieldDto.toEntity();
                        entity.setResourceGroup(group); // FK 설정
                        return entity;
                    })
                    .toList();

            customFieldRepository.saveAll(fields);
        }
    }


    // -------------------- 리소스 그룹 목록 조회 --------------------
    public ResourceGroupDto.ResourceGroupListRes getResourceGroupsByCompanyId(Long companyId) {

        // 특정 기업(companyId)에 속한 모든 리소스 그룹을 조회
        List<ResourceGroups> groups = resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);

        // Entity -> DTO 변환
        List<ResourceGroupDto.ResourceGroupDetailRes> dtoList = groups.stream()
                .map(ResourceGroupDto.ResourceGroupDetailRes::fromEntity)
                .collect(Collectors.toList());

        // List로 만들어서 반환
        return ResourceGroupDto.ResourceGroupListRes.builder()
                .resourceGroups(dtoList)
                .build();
    }


    // -------------------- 리소스 그룹 조회 --------------------
    @Transactional(readOnly = true)
    public ResourceGroupDto.ResourceGroupDetailRes getResourceGroupById(Long resourceGroupId) {
        ResourceGroups group = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 서비스 그룹이 존재하지 않습니다."));

        return ResourceGroupDto.ResourceGroupDetailRes.fromEntity(group);
    }


    // -------------------- 리소스 그룹 조회 (수정용) --------------------
    @Transactional(readOnly = true)
    public ResourceGroupDto.ResourceGroupUpdateRes getResourceGroupUpdateDetail(Long resourceGroupId) {
        ResourceGroups group = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        return ResourceGroupDto.ResourceGroupUpdateRes.fromEntity(group);
    }


    // -------------------- 리소스 그룹 수정 --------------------
    @Transactional
    public void updateResourceGroup(Long userId,
            Long resourceGroupId, ResourceGroupDto.ResourceGroupUpdateReq dto) {
        ResourceGroups resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));


        // --- 커스텀 필드 처리 ---
        List<CustomFieldDefinitions> newCustomFields = dto.getCustomFields() != null
                ? dto.getCustomFields().stream()
                .map(CustomFieldDto.CustomFieldReq::toEntity)
                .collect(Collectors.toList())
                : Collections.emptyList();

        // 기존 컬렉션 유지, clear 후 새 객체 추가
        resourceGroup.getCustomFieldDefinitions().clear();
        for (CustomFieldDefinitions field : newCustomFields) {
            field.setResourceGroup(resourceGroup); // 양방향 관계 설정
            resourceGroup.getCustomFieldDefinitions().add(field);
        }

        resourceGroup.update(
                dto.getName(),
                dto.getDescription(),
                dto.getThumbnail(),
                dto.getCategory(),
                dto.getIsAlwaysAvailable(),
                user

        );
    }


    // -------------------- 리소스 그룹 삭제 --------------------
    @Transactional
    public void deleteResourceGroup(Long userId, Long resourceGroupId) {
        try {
            ResourceGroups resourceGroup = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            if (!Boolean.TRUE.equals(resourceGroup.getIsActive())) {
                throw new IllegalArgumentException("이미 비활성화 또는 삭제된 리소스 그룹입니다.");
            }

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

            resourceGroup.setIsActive(false);
            resourceGroup.softDelete();
            resourceGroup.setUpdatedBy(user);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", e);
        }
    }


    // -------------------- 리소스 그룹의 정보 반환 (리소스 생성에 필요한 필드 조회를 위함) --------------------
    @Transactional(readOnly = true)
    public ResourceGroupDto.ServiceRegisterFieldRes getServiceRegisterField(Long resourceGroupId) {
        ResourceGroups resourceGroup = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        return ResourceGroupDto.ServiceRegisterFieldRes.fromEntity(resourceGroup);
    }


    // -------------------- 리소스 그룹 활성화  --------------------
    @Transactional
    public void activate(Long resourceGroupId) {

        try {
            // TODO : 플랫폼 관리자 권한을 가졌는지 확인

            ResourceGroups resourceGroup = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            if (Boolean.TRUE.equals(resourceGroup.getIsActive())) {
                log.info("이미 활성화된 서비스 그룹입니다. id={}", resourceGroupId);
                return;
            }

            resourceGroup.setIsActive(true);
            // resourceGroup.setUpdatedBy(user); // 수정자 추후 추가

            log.info("서비스 그룹 활성화 완료 - id={}", resourceGroupId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // -------------------- 리소스 그룹 비활성화  --------------------
    @Transactional
    public void deactivate(Long resourceGroupId) {
        try {
            ResourceGroups resourceGroup = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));


            if (Boolean.FALSE.equals(resourceGroup.getIsActive())) {
                log.info("이미 비활성화된 서비스 그룹입니다. id={}", resourceGroupId);
                return;
            }

            resourceGroup.setIsActive(false);
            // resourceGroup.setUpdatedBy(user); // 수정자 추후 추가

            log.info("서비스 그룹 비활성화 완료 - id={}", resourceGroupId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }
}
