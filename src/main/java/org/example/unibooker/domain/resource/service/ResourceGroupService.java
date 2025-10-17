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

@Service
@RequiredArgsConstructor
public class ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

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
}
