package io.zumely.gateway.resume.application.service;

import io.zumely.gateway.resume.application.event.Event;

public interface EventSerializer {

    <T extends Event> String serialize(T event);

    <T extends Event> T deserialize(String json);
}
