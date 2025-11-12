package org.example.apiresource.resource;

import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.entity.*;
import org.example.apiresource.usecase.port.in.ResourceWebPort;
import org.example.apiresource.usecase.port.out.ResourcePersistencePort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.example.apiresource.usecase.port.out.ResourceCustomFieldPersistencePort;
import org.example.apiresource.usecase.port.out.CustomFieldDefinitionPersistencePort;
import org.example.common.user.AuthDto;
import org.example.common.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class ResourceRegisterIntegrationTest {

    @Autowired
    private ResourceWebPort resourceCommand;

    @Autowired
    private ResourceGroupPersistencePort resourceGroupPersistencePort;

    private ResourceGroups group;
    private Long userId = 1L; // DB에 존재하는 사용자 ID
    private CustomFieldDefinitions field1;

    @BeforeEach
    void setUp() {
        // 리소스 그룹 준비
        group = ResourceGroups.builder()
                .name("Test Group")
                .category(ServiceCategory.SEAT)
                .isAlwaysAvailable(true)
                .build();
        resourceGroupPersistencePort.saveResourceGroup(group);

        // 커스텀 필드 준비
        field1 = CustomFieldDefinitions.builder()
                .fieldName("Test Field")
                .targetType(CustomTargetType.RESOURCE)
                .build();
        resourceGroupPersistencePort.saveCustomField(field1);
    }

    @Test
    void testRegisterResource() {
        // given
        ResourceDto.ResourceRegisterReq dto = ResourceDto.ResourceRegisterReq.builder()
                .name("Resource 1")
                .resourceGroupId(group.getId())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .timeInterval(30)
                .capacity(10)
                .row(2)
                .col(5)
                .customFieldValues(List.of(
                        CustomFieldDto.CustomFieldValue.builder()
                                .customFieldId(field1.getId())
                                .values(List.of("Value1", "Value2"))
                                .build()
                ))
                .build();

        // when
        resourceCommand.register(dto, userId);

    }
}
