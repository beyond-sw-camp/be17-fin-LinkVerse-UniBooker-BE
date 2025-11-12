package org.example.apireservation.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.CustomFieldValue;
import org.example.apireservation.infrastructure.CustomFieldValueExternalPort;
import org.example.apireservation.usecase.port.in.CustomFieldValueCommand;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldValueExternalAdapter implements CustomFieldValueExternalPort {
    @Override
    public List<CustomFieldValue> getUserFieldValuesByReservation(Long reservationId) {
        return List.of();
    }

    @Override
    public List<CustomFieldValue> register(Long reservatinId, List<CustomFieldValueCommand> dto) {
        return List.of();
    }
}
