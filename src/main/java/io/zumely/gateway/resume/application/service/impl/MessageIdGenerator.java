package io.zumely.gateway.resume.application.service.impl;

import io.zumely.gateway.resume.application.service.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageIdGenerator implements IdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
