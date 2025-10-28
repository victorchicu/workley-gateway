package ai.workley.gateway.features.chat.infra.id;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RandomIdGenerator implements IdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
