package org.example.apiresource.resource;

import org.example.apiresource.adapter.out.repository.CustomFieldDefinitionRepository;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.usecase.port.out.CustomFieldDefinitionPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class CustomFieldDefinitionPersistenceAdapterTest {

    @Autowired
    private CustomFieldDefinitionPersistencePort customFieldDefinitionPersistencePort;

    @Autowired
    private CustomFieldDefinitionRepository repository;

    private CustomFieldDefinitions field1;
    private CustomFieldDefinitions field2;

    @BeforeEach
    void setUp() {
        // 미리 DB에 넣어둠
        field1 = CustomFieldDefinitions.builder()
                .fieldName("Field1")
                .targetType(CustomTargetType.RESOURCE)
                .build();

        field2 = CustomFieldDefinitions.builder()
                .fieldName("Field2")
                .targetType(CustomTargetType.RESOURCE)
                .build();

        repository.save(field1);
        repository.save(field2);
    }

    @Test
    void testFindAllByIdsAndNotDeleted() {
        // given
        List<Long> ids = List.of(field1.getId(), field2.getId());

        // when
        List<CustomFieldDefinitions> results = customFieldDefinitionPersistencePort.findAllByIdsAndNotDeleted(ids);

        // then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(field1));
        assertTrue(results.contains(field2));
    }

    @Test
    void testFindAllByIdsAndNotDeleted_DeletedField() {
        // given: field2를 soft delete 처리
        field2.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(field2);

        List<Long> ids = List.of(field1.getId(), field2.getId());

        // when
        List<CustomFieldDefinitions> results = customFieldDefinitionPersistencePort.findAllByIdsAndNotDeleted(ids);

        // then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(field1));
        assertFalse(results.contains(field2));
    }
}