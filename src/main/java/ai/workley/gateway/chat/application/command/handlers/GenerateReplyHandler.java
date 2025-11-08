package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.payloads.GenerateReplyPayload;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GenerateReplyHandler implements CommandHandler<GenerateReply, GenerateReplyPayload> {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyHandler.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public GenerateReplyHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<GenerateReply> supported() {
        return GenerateReply.class;
    }

    @Override
    public Mono<GenerateReplyPayload> handle(String actor, GenerateReply command) {
        return Mono.defer(() -> {
            applicationEventPublisher.publishEvent(
                    new ReplyStarted(
                            actor, command.chatId(), command.message()));
            return Mono.just(GenerateReplyPayload.ack());
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate message. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}