package org.example.apiresource.resourceGroup;

import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.example.common.user.AuthDto;
import org.example.common.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class ResourceGroupDeactivateIntegrationTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupWebPort;

    @Autowired
    private ResourceGroupPersistencePort resourceGroupPersistencePort;

    private final Long existingResourceGroupId = 3L; // DB에 존재하는 리소스 그룹 ID

    @Test
    void deactivateResourceGroupTest() {
        // given: 플랫폼 관리자 권한 사용자 생성
        AuthDto authDto = AuthDto.builder()
                .id(1L) // DB에 존재하는 사용자 ID
                .role(UserRole.SUPER)
                .build();

        // when: 서비스 그룹 비활성화 실행
        resourceGroupWebPort.deactivate(authDto, existingResourceGroupId);

        // then: DB에서 다시 조회하여 검증
        Optional<ResourceGroups> optionalGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(existingResourceGroupId);
        assertTrue(optionalGroup.isPresent(), "리소스 그룹이 존재해야 합니다.");

        ResourceGroups group = optionalGroup.get();

        assertFalse(group.getIsActive(), "리소스 그룹은 비활성화되어야 합니다.");
        assertNotNull(group.getUpdatedBy(), "수정자가 설정되어야 합니다.");

        System.out.println("🚫 비활성화 완료: id=" + group.getId() +
                ", isActive=" + group.getIsActive() +
                ", updatedBy=" + group.getUpdatedBy());
    }
}
