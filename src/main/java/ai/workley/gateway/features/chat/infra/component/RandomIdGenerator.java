package ai.workley.gateway.features.chat.infra.component;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
