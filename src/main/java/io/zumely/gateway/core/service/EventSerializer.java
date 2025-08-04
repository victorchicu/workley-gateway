package io.zumely.gateway.core.service;

import io.zumely.gateway.resume.application.event.Event;

public interface EventSerializer {

    <T extends Event> String serialize(T event);

    <T extends Event> T deserialize(String json);
}