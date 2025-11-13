package org.example.apistatistics.usecase.port.in;

import org.example.apistatistics.domain.model.dto.DashboardDto;

public interface DashboardWebPort {

    DashboardDto.AdminDashboardResponse getCompanyDashboard(Long companyId);
}
