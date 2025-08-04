package io.zumely.gateway.resume.application.service.impl;

import io.zumely.gateway.resume.application.service.AggregateIdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AggregateIdGeneratorImpl implements AggregateIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
