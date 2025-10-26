package ai.jobbortunity.gateway.chat.domain.model;

import org.springframework.context.PayloadApplicationEvent;

import java.security.Principal;

public interface EventSerializer {

    <T extends PayloadApplicationEvent<Principal>> String serialize(T event);

    <T extends PayloadApplicationEvent<Principal>> T deserialize(String json, Class<T> clazz);
}
