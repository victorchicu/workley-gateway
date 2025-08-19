package io.zumely.gateway.resume.application.service.impl;

import io.zumely.gateway.resume.application.service.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
