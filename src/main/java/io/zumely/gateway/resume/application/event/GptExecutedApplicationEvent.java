package io.zumely.gateway.resume.application.event;

import java.security.Principal;

public record GptExecutedApplicationEvent(Principal actor, String chatId, String prompt) implements ApplicationEvent {

}
