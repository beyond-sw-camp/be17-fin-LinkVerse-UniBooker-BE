package org.example.apiresource.resourceGroup;

import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class ResourceGroupGetServiceRegisterFieldIntegrationTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupWebPort;

    // 테스트용 — 실제 존재하는 리소스 그룹 ID 사용
    private final Long existingResourceGroupId = 3L;

    @Test
    void getServiceRegisterFieldTest() {
        // when
        ResourceGroupDto.ServiceRegisterFieldRes res =
                resourceGroupWebPort.getServiceRegisterField(existingResourceGroupId);

        // then
        assertNotNull(res, "응답이 null이면 안 됩니다.");
        System.out.println("이름: " + res.getName());
        System.out.println("카테고리: " + res.getCategory());
        System.out.println("상시 모집 여부: " + res.getIsAlwaysAvailable());
        System.out.println("커스텀 필드 수: " + res.getCustomFields().size());

        // 필수 필드 검증
        assertNotNull(res.getName(), "리소스 그룹 이름이 null이면 안 됩니다.");
        assertNotNull(res.getCategory(), "카테고리가 null이면 안 됩니다.");

        // 커스텀 필드가 있다면 필드 이름 확인
        if (!res.getCustomFields().isEmpty()) {
            var field = res.getCustomFields().get(0);
            assertNotNull(field.getFieldName(), "커스텀 필드 이름이 null이면 안 됩니다.");
            System.out.println("첫 번째 필드 이름: " + field.getFieldName());
            System.out.println("데이터 타입: " + field.getDataType());
            System.out.println("타겟 타입: " + field.getTargetType());
            System.out.println("필수 여부: " + field.getRequired());
        }
    }
}
