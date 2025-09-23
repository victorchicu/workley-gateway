package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record ReplyGeneratedApplicationEvent(Principal actor, String chatId, String prompt) implements ApplicationEvent {

}
