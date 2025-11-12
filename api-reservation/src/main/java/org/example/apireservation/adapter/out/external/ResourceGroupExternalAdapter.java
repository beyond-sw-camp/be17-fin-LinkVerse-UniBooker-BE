package org.example.apireservation.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.ResourceGroup;
import org.example.apireservation.infrastructure.ResourceGroupExternalPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceGroupExternalAdapter implements ResourceGroupExternalPort {
    @Override
    public Optional<ResourceGroup> findByIdAndDeletedAtIsNull(Long resourceGroupId) {
        return null;
    }
}
