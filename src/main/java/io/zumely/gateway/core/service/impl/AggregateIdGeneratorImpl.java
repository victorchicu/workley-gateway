package io.zumely.gateway.core.service.impl;

import io.zumely.gateway.core.service.AggregateIdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AggregateIdGeneratorImpl implements AggregateIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
