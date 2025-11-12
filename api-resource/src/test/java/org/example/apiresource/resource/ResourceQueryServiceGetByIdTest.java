package org.example.apiresource.resource;


import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.usecase.impl.ResourceUseCase;
import org.example.apiresource.usecase.port.out.ResourcePersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResourceQueryServiceImplGetByIdTest {

    @Autowired
    private ResourceUseCase resourceQueryService; // Impl 직접 주입

    @Autowired
    private ResourcePersistencePort resourcePersistencePort;

    @Test
    void getResourceByIdTest() {
        Long testResourceId = 1L; // DB에 존재하는 리소스 ID

        // 단건 조회
        ResourceDto.ResourceDetailInfo detail = resourceQueryService.getResourceById(testResourceId);

        assertNotNull(detail, "리소스 상세 정보는 null이 아니어야 합니다.");
        assertEquals(testResourceId, detail.getId(), "조회된 리소스 ID가 일치해야 합니다.");

        // 화면에 출력
        System.out.println("✅ 리소스 상세 조회 결과:");
        System.out.println("ID: " + detail.getId());
        System.out.println("이름: " + detail.getName());
        System.out.println("설명: " + detail.getDescription());
        System.out.println("그룹 ID: " + detail.getResourceGroupId());
        System.out.println("그룹 이름: " + detail.getResourceGroupName());
        System.out.println("상태: " + detail.getStatus());
        System.out.println("시작일: " + detail.getStartDate());
        System.out.println("종료일: " + detail.getEndDate());
        System.out.println("시간 간격: " + detail.getTimeInterval());
        System.out.println("용량: " + detail.getCapacity());
        System.out.println("행: " + detail.getRow() + ", 열: " + detail.getCol());
        System.out.println("카테고리: " + detail.getCategory());
        System.out.println("상시 사용 가능 여부: " + detail.getIsAlwaysAvailable());
        System.out.println("버전: " + detail.getVersion());
    }
}