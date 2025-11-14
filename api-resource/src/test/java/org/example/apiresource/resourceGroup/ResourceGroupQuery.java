package org.example.apiresource.resourceGroup;

import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.common.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ResourceGroupQuery {
    @Autowired
    private ResourceGroupWebPort resourceGroupQuery; // Usecase Impl (Port In)

    @Test
    void testGetResourceGroupsByCompanyId() {
        Long testCompanyId = 1L;
        UserRole role = UserRole.ADMIN; // ADMIN, USER 등 테스트 가능

        // 조회
        ResourceGroupDto.ResourceGroupListRes listRes =
                resourceGroupQuery.getResourceGroupsByCompanyId(role, testCompanyId);

        List<ResourceGroupDto.ResourceGroupDetailRes> groups = listRes.getResourceGroups();

        // 단순 출력 확인
        System.out.println("==== 리소스 목록 ====");
        for (ResourceGroupDto.ResourceGroupDetailRes group : groups) {
            System.out.printf("ID: %d, 이름: %s, 코드: %s, 설명: %s, 카테고리: %s, 썸네일: %s%n",
                    group.getId(),
                    group.getName(),
                    group.getGroupCode(),
                    group.getDescription(),
                    group.getCategory(),
                    group.getThumbnail());
        }

        // 간단한 단언 예시
        assertNotNull(groups);
        assertTrue(groups.size() > 0, "리소스 그룹이 하나 이상 존재해야 함");
    }
}
