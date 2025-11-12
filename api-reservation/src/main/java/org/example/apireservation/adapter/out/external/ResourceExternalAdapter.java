package org.example.apireservation.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.Resource;
import org.example.apireservation.infrastructure.ResourceExternalPort;
import org.springframework.stereotype.Component;

import java.util.Optional;


/** Circuit Breaker 통한 MSA 통신 */
@Component
@RequiredArgsConstructor
public class ResourceExternalAdapter implements ResourceExternalPort {

    @Override
    public Optional<Resource> findById(Long resourceId) {
        return Optional.empty();
    }

    @Override
    public Optional<Resource> findResourceById(Long resourceId) {
        return null;
    }

    @Override
    public Optional<Resource> findResourceByIdForUpdate(Long resourceId) {
        return null;
    }
}
