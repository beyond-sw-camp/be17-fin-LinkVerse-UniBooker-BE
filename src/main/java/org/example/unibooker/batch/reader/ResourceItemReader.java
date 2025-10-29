package org.example.unibooker.batch.reader;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceItemReader implements ItemReader<Resources> {

    private final ResourceRepository resourceRepository;
    private Iterator<Resources> resourceIterator;



    @Override
    public Resources read() throws Exception {
        if (resourceIterator == null) {
            // 처음 호출될 때 DB에서 날짜가 설정된 리소스만 조회
            List<Resources> resources = resourceRepository.findByDeletedAtIsNullAndStartDateIsNotNullAndEndDateIsNotNull();
            resourceIterator = resources.iterator();
        }
        return resourceIterator.hasNext() ? resourceIterator.next() : null;
    }
}

