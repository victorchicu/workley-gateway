package io.zumely.gateway.resume.application.service;

import io.zumely.gateway.resume.application.event.ApplicationEvent;

public interface EventSerializer {

    <T extends ApplicationEvent> String serialize(T event);

    <T extends ApplicationEvent> T deserialize(String json);
}
