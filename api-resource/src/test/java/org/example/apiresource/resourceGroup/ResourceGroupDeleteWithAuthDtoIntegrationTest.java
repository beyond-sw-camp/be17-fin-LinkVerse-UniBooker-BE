package org.example.apiresource.resourceGroup;

import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.example.common.model.dto.AuthDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback(false)
public class ResourceGroupDeleteWithAuthDtoIntegrationTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupWebPort;

    @Autowired
    private ResourceGroupPersistencePort resourceGroupQueryPort;

    private final Long existingResourceGroupId = 2L;

    @Test
    void deleteResourceGroupWithAuthDtoTest() {
        AuthDto authDto = AuthDto.builder()
                .id(1L)
                .role(null)
                .build();

        // 삭제 수행
        resourceGroupWebPort.deleteResourceGroup(authDto.getId(), existingResourceGroupId);

    }
}
