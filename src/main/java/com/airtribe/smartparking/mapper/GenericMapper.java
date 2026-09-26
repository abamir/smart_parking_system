package com.airtribe.smartparking.mapper;

import java.util.Collection;
import java.util.List;

public interface GenericMapper {


    /**
     * Maps a source object to the specified destination type.
     *
     * @param source          the source object
     * @param destinationType destination class
     * @return mapped object
     */
    <D> D map(Object source, Class<D> destinationType);

    /**
     * Maps a collection of source objects to a destination type.
     *
     * @param sourceList      list of source objects
     * @param destinationType destination class
     * @return mapped list
     */
    <S, D> List<D> mapList(Collection<S> sourceList, Class<D> destinationType);


}
