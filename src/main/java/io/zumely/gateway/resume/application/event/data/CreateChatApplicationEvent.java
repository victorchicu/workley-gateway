package io.zumely.gateway.resume.application.event.data;

import io.zumely.gateway.resume.application.command.data.Message;
import io.zumely.gateway.resume.application.command.data.Prompt;

import java.security.Principal;

public record CreateChatApplicationEvent(Principal actor, String chatId, Message<Prompt> message) implements ApplicationEvent {
}
