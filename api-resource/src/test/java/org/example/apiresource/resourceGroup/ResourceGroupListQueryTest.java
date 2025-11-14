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

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ResourceGroupListQueryTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupQuery;

    @Test
    void testGetResourceGroupsByCompanyId() {
        Long testCompanyId = 1L; // 생성 테스트에서 사용한 회사 ID
        UserRole role = UserRole.ADMIN; // 조회할 역할 (ADMIN, USER 등)

        ResourceGroupDto.ResourceGroupListRes listRes =
                resourceGroupQuery.getResourceGroupsByCompanyId(role, testCompanyId);

        List<ResourceGroupDto.ResourceGroupDetailRes> groups = listRes.getResourceGroups();

        System.out.println("==== 리소스 그룹 목록 ====");
        for (ResourceGroupDto.ResourceGroupDetailRes group : groups) {
            System.out.println("ID: " + group.getId());
            System.out.println("이름: " + group.getName());
            System.out.println("코드: " + group.getGroupCode());
            System.out.println("설명: " + group.getDescription());
            System.out.println("카테고리: " + group.getCategory());
            System.out.println("썸네일: " + group.getThumbnail());
            System.out.println("생성일: " + group.getCreatedAt());
            System.out.println("수정일: " + group.getUpdatedAt());
            System.out.println("관리자: " + group.getAdministrator());
            System.out.println("수정자: " + group.getUpdatedByName());
            System.out.println("서비스 수: " + group.getServiceCount());
            System.out.println("진행중 서비스 수: " + group.getActiveServiceCount());
            System.out.println("활성 상태: " + group.getIsActive());
            System.out.println("-----------------------------");
        }
    }
}
