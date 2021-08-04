package com.chinadaas.service.impl;

import com.chinadaas.entity.SuperCorporationEntity;
import com.chinadaas.repository.SuperCorporationRepository;
import com.chinadaas.service.SuperCorporationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author lawliet
 * @version 1.0.0
 * @description
 * @createTime 2021.07.29
 */
@Slf4j
@Service
public class SuperCorporationServiceImpl implements SuperCorporationService {

    private final SuperCorporationRepository repository;

    @Autowired
    public SuperCorporationServiceImpl(SuperCorporationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void insertSuperCorporation(SuperCorporationEntity superCorporationEntity) {
        repository.insertSuperCorporation(superCorporationEntity);
    }

    @Override
    public Set<String> extraParentIds() {
        return repository.extraParentIds();
    }
}
