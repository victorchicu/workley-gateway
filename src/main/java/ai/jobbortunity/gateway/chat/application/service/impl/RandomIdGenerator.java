package ai.jobbortunity.gateway.chat.application.service.impl;

import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
