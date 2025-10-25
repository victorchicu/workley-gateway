package ai.workley.gateway.chat.infrastructure.service;

import ai.workley.gateway.chat.domain.model.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
