package org.example.apiresource.resource;


import org.example.apiresource.domain.model.ResourceStatus;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.entity.Resources;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.usecase.impl.ResourceUseCase;
import org.example.apiresource.usecase.port.out.ResourcePersistencePort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class ResourceQueryIntegrationTest {

    @Autowired
    private ResourceGroupPersistencePort resourceGroupPersistencePort;

    @Autowired
    private ResourcePersistencePort resourcePersistencePort;

    @Autowired
    private ResourceUseCase resourceQuery; // ServiceImpl 호출용

    private Long testGroupId;

    @BeforeEach
    void setup() {
        // 테스트용 서비스 그룹 생성
        ResourceGroups group = ResourceGroups.builder()
                .name("테스트 그룹")
                .category(ServiceCategory.EVENT)
                .isAlwaysAvailable(true)
                .isActive(true)
                .build();
        resourceGroupPersistencePort.saveResourceGroup(group);
        testGroupId = group.getId();

        // 테스트용 리소스 생성
        for (int i = 1; i <= 3; i++) {
            Resources resource = Resources.builder()
                    .name("리소스 " + i)
                    .description("테스트 리소스 " + i)
                    .resourceGroup(group)
                    .isActive(true)
                    .capacity(4)
                    .status(ResourceStatus.IN_PROGRESS)
                    .build();
            resourcePersistencePort.createResource(resource);
        }
    }

    @Test
    void getAllResourcesByGroupIdAndPrintTest() {
        // when: ServiceImpl 호출
        ResourceDto.ResourceListRes listRes = resourceQuery.getAllResourcesByGroupId(testGroupId);

        // then: 검증
        assertNotNull(listRes);
        assertEquals(3, listRes.getResources().size(), "리소스 개수가 맞아야 합니다.");

        // 화면 출력용
        System.out.println("=== 서비스 그룹 ID: " + testGroupId + " 리소스 목록 ===");
        for (ResourceDto.ResourceDetailInfo res : listRes.getResources()) {
            System.out.println("리소스 ID: " + res.getId()
                    + ", 이름: " + res.getName()
                    + ", 설명: " + res.getDescription()
                    + ", 상태: " + res.getStatus()
                    + ", 수정일: " + res.getUpdatedAt());
        }
    }
}
