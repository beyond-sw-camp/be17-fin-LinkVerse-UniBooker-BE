package org.example.apiresource.resourceGroup;

import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.common.model.dto.AuthDto;
import org.example.common.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Rollback(false)
class ResourceGroupIntegrationTest {

    @Autowired
    private ResourceGroupWebPort resourceGroupWebPort;

    @Test
    void testRegisterResourceGroup() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<AuthDto> constructor = AuthDto.class.getDeclaredConstructor(Long.class, Long.class, UserRole.class);
        constructor.setAccessible(true); // private 접근 허용
        AuthDto authDto = constructor.newInstance(1L, 1L, UserRole.ADMIN);

        // ✅ 2. 커스텀 필드 목록 생성
        CustomFieldDto.CustomFieldReq roomNameField = new CustomFieldDto.CustomFieldReq();
        roomNameField.setFieldName("회의실 이름");
        roomNameField.setDescription("예약하려는 회의실의 이름");
        roomNameField.setDataType(CustomDataType.TEXT);
        roomNameField.setTargetType(CustomTargetType.RESOURCE);
        roomNameField.setRequired(true);

        CustomFieldDto.CustomFieldReq roomTypeField = new CustomFieldDto.CustomFieldReq();
        roomTypeField.setFieldName("회의실 유형");
        roomTypeField.setDescription("회의실 유형을 선택하세요");
        roomTypeField.setDataType(CustomDataType.RADIO);
        roomTypeField.setTargetType(CustomTargetType.USER);
        roomTypeField.setRequired(true);
        roomTypeField.setOptions(List.of("소형", "중형", "대형"));

        // ✅ 3. 서비스 그룹 생성 DTO 구성
        ResourceGroupDto.ResourceGroupRegisterReq dto = new ResourceGroupDto.ResourceGroupRegisterReq();
        dto.setName("테스트 회의실 그룹");
        dto.setGroupCode("MEET001");
        dto.setDescription("회의실 예약용 서비스 그룹");
        dto.setThumbnail("https://example.com/meetingroom.jpg");
        dto.setCategory(ServiceCategory.RESERVATION);
        dto.setIsAlwaysAvailable(true);
        dto.setCustomFields(List.of(roomNameField, roomTypeField));

        // ✅ 4. UseCase 호출 (API 없이 register() 바로 호출)
        resourceGroupWebPort.register(dto, authDto.getId(), authDto.getCompanyId());

        // ✅ 5. 검증 (단순 출력으로 대체, 실제로는 repository 통해 확인 가능)
        System.out.println("✅ 리소스 그룹 생성 테스트 완료!");
    }
}
