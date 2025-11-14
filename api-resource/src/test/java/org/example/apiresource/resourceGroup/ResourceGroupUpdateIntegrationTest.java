package org.example.apiresource.resourceGroup;

import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.common.model.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Rollback(false)
class ResourceGroupUpdateIntegrationTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupWebPort;

    @Test
    void updateResourceGroupIntegrationTest() {
        // 준비: DB에 미리 존재하는 리소스 그룹 ID와 사용자 ID
        Long existingResourceGroupId = 2L;
        Long existingUserId = 1L;

        // DTO 생성
        ResourceGroupDto.ResourceGroupUpdateReq updateReq = ResourceGroupDto.ResourceGroupUpdateReq.builder()
                .name("Updated Name")
                .groupCode("UPD01")
                .description("Updated Description")
                .thumbnail("updated.png")
                .category(String.valueOf(ServiceCategory.SEAT))
                .isAlwaysAvailable(true)
                .customFields(List.of(
                        CustomFieldDto.CustomFieldReq.builder()
                                .fieldName("newField1")
                                .dataType(CustomDataType.TEXT)
                                .targetType(CustomTargetType.RESOURCE)
                                .required(true)
                                .description("New Field Description")
                                .build()
                ))
                .build();

        // 인증 DTO
        AuthDto authDto = AuthDto.builder().id(existingUserId).build();

        // 수행
        resourceGroupWebPort.updateResourceGroup(authDto, existingResourceGroupId, updateReq);

        // 검증: DB에서 실제 엔티티 조회
        ResourceGroupDto.ResourceGroupDetailRes updatedGroup =
                resourceGroupWebPort.getResourceGroupById(authDto, existingResourceGroupId);

        assertEquals("Updated Name", updatedGroup.getName());
        assertEquals("UPD01", updatedGroup.getGroupCode());
        assertEquals("Updated Description", updatedGroup.getDescription());

//        assertEquals(1, updatedGroup.getCustomFieldDefinitions().size());
//        CustomFieldDefinitions field = updatedGroup.getCustomFieldDefinitions().get(0);
//        assertEquals("newField1", field.getFieldName());
//        assertEquals("STRING", field.getDataType().name());
//        assertEquals("RESOURCE", field.getTargetType().name());
//        assertTrue(field.getIsRequired());
//        assertEquals("New Field Description", field.getDescription());
    }
}
