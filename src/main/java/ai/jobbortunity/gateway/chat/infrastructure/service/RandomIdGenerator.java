package ai.jobbortunity.gateway.chat.infrastructure.service;

import ai.jobbortunity.gateway.chat.domain.model.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
