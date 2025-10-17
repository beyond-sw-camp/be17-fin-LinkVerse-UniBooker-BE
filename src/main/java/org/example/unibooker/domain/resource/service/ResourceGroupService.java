package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    // -------------------- 리소스 그룹 등록 --------------------
    public void register(ResourceGroupDto.ResourceGroupRegisterReq dto, Long userId) {
        // 리소스 그룹 이름 중복 체크 (같은 회사 내 동일 이름 방지)
        if (resourceGroupRepository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())) {
            throw new IllegalArgumentException("이미 동일한 이름의 서비스 그룹이 존재합니다.");
        }

        // 기업 엔티티 조회
        Companies company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기업 ID입니다."));

        // 사용자 엔티티 조회
        Users authUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다."));

        // DTO → Entity 변환
        ResourceGroups group = dto.toEntity(authUser, company);

        resourceGroupRepository.save(group);
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
    public void updateResourceGroup(Long resourceGroupId, ResourceGroupDto.ResourceGroupUpdateReq dto, Long userId) {
        ResourceGroups resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        resourceGroup.update(
                dto.getName(),
                dto.getDescription(),
                dto.getThumbnail(),
                dto.getCategory(),
                dto.getIsAlwaysAvailable(),
                user
        );
    }
}
