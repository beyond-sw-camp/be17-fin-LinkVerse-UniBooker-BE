package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.ResourceDto;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.service.ResourceService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리소스 관리", description = "리소스에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceController {
    private final ResourceService resourceService;

    // ---------------- 생성 ----------------
    @Operation(summary = "서비스 생성", description = "예약/신청 서비스를 생성합니다.")
    @PostMapping
    public BaseResponse register(@RequestBody ResourceDto.ResourceRegisterReq dto) {
        resourceService.register(dto);
        return BaseResponse.success("서비스가 생성되었습니다.");
    }


    // ---------------- 목록 조회 ----------------
    @Operation(summary = "서비스 목록 조회", description = "서비스 목록을 조회합니다.")
    @GetMapping("/{serviceGroupId}")
    public void getAllResources() {
        // TODO: 서비스 그룹 수정 컨트롤러 구현
    }


    // ---------------- 단건 조회 ----------------
    @Operation(summary = "서비스 상세 조회", description = "서비스를 상세 조회합니다.")
    @GetMapping("/{resourceId}")
    public void getResourceById() {
        // TODO: 서비스 그룹 수정 컨트롤러 구현
    }


    // ---------------- 수정용 상세 조회 ----------------
    @Operation(summary = "서비스 상세 조회(수정용)", description = "서비스 생성할 때 입력한 데이터 전체를 조회합니다.")
    @GetMapping("/{resourceGroupId}/edit")
    public void getResourceDetailById() {
        // TODO: 서비스 그룹 수정 컨트롤러 구현
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "서비스 수정", description = "기존의 예약/신청 서비스를 수정합니다.")
    @PutMapping("/{resourceId}")
    public void update() {
        // TODO: 서비스 그룹 수정 컨트롤러 구현
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "서비스 삭제", description = "기존의 예약/신청 서비스를 삭제합니다.")
    @DeleteMapping("/{resourceId}")
    public void delete() {
        // TODO: 서비스 그룹 삭제 컨트롤러 구현
    }


    // ---------------- 서비스 활성화 상태 변경 ----------------
    @Operation(summary = "서비스의 활성화 상태를 변경", description = "서비스의 활성화 상태를 변경합니다.")
    @GetMapping("active/{resourceId}")
    public void ServiceActivationToggle(@PathVariable Long resourceId,
                                        @RequestParam Boolean isActive) {
        // TODO
    }
}
