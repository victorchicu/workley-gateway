package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

public record IdentifyIntentEvent(String actor, String chatId, Message<String> message) implements ApplicationEvent {

}
