package io.zumely.gateway.resume.application.service.impl;

import io.zumely.gateway.resume.application.service.ChatIdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatIdGeneratorImpl implements ChatIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}