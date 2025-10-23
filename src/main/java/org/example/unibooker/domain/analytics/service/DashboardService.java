package org.example.unibooker.domain.analytics.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.analytics.model.DashboardDto;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ResourceGroupRepository resourceGroupRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public DashboardDto.DashboardResponse getCompanyDashboardData(Long companyId) {
        // TODO: 대시보드 데이터 조회 로직 구현

        // 1. 해당 회사(companyId)에 속한 리소스 그룹 목록 조회
        List<ResourceGroups> resourceGroupList = resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);

        // 2. 각 리소스 그룹별 예약 수, 리소스 수, 조회 수 집계
        //    - ReservationRepository.countByServiceGroup(...)
        //    - group.getResources().size()
        //    - ResourceStatistics 또는 group.getViewCount() 활용
        int activeServiceGroups = resourceGroupList.size();
        int activeServices = resourceRepository.findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(companyId).size();
        int userCount = userRepository.findAllByCompanyIdAndRole(companyId, UserRole.USER).size();

        // 3. Summary 데이터 구성
        //    - totalReservations: 회사 전체 예약 수
        //    - activeServiceGroups: 활성화된 그룹 수
        //    - activeServices: 활성 리소스 총합
        //    - userCount: 해당 회사에 소속된 사용자 수

        // 4. ServiceGroups 리스트 구성
        //    - 각 그룹별 이름, 서비스 수, 예약 수, 상태, 조회 수

        // 5. ReservationTrends 데이터 구성 (예: 최근 7일)
        //    - 날짜별로 그룹별 예약 수 집계
        //    - ReservationRepository 또는 ResourceStatistics 기반

        // 6. DashboardResponse DTO 빌드 및 반환
        //    - new DashboardResponse(summary, serviceGroups, reservationTrends)

        return null;
    }
}
