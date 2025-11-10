package org.example.apiresource.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.usecase.port.in.ResourceGroupUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceGroupImpl implements ResourceGroupUseCase {
    private final ResourceGroupRepository resourceGroupRepository;

    @Override
    @Transactional
    public void register(ResourceGroupDto.ResourceGroupRedisterReq dto, Long userId, Long companyId) {

    }
}
