package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record AssistantReplyAddedApplicationEvent(Principal actor, String chatId, String prompt) implements ApplicationEvent {

}
