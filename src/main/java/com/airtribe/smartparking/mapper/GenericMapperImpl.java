package com.airtribe.smartparking.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GenericMapperImpl implements GenericMapper {

    private final ModelMapper modelMapper;

    @Override
    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }

        return modelMapper.map(source, destinationType);
    }

    @Override
    public <S, D> List<D> mapList(Collection<S> sourceList, Class<D> destinationType) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(element -> modelMapper.map(element, destinationType))
                .toList();
    }
}
