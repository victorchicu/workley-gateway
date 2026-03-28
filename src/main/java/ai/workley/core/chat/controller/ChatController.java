package ai.workley.core.chat.controller;

import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.ErrorPayload;
import ai.workley.core.chat.model.Payload;
import ai.workley.core.chat.service.ChatService;
import ai.workley.core.idempotency.IdempotencyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public record CreateChatRequest(String prompt) {}

    public record AddMessageRequest(String text) {}

    @PostMapping
    @IdempotencyKey
    public Mono<ResponseEntity<Payload>> createChat(Principal actor, @RequestBody CreateChatRequest request) {
        return Mono.deferContextual(contextView -> {
            log.info("Create chat (actor={})", actor.getName());

            return chatService.createChat(actor.getName(), request.prompt())
                    .map(payload -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Payload) payload))
                    .onErrorResume(ApplicationError.class, error ->
                            Mono.just(ResponseEntity.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(new ErrorPayload(error.getMessage()))));
        });
    }

    @PostMapping("/{chatId}/messages")
    @IdempotencyKey
    public Mono<ResponseEntity<Payload>> addMessage(Principal actor, @PathVariable String chatId,
                                                     @RequestBody AddMessageRequest request) {
        return Mono.deferContextual(contextView -> {
            log.info("Add message (actor={}, chatId={})", actor.getName(), chatId);

            return chatService.addMessage(actor.getName(), chatId, request.text())
                    .map(payload -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body((Payload) payload))
                    .onErrorResume(ApplicationError.class, error ->
                            Mono.just(ResponseEntity.badRequest()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(new ErrorPayload(error.getMessage()))));
        });
    }

    @GetMapping("/{chatId}")
    public Mono<ResponseEntity<Payload>> getChat(Principal actor, @PathVariable String chatId) {
        log.info("Get chat (actor={}, chatId={})", actor.getName(), chatId);

        return chatService.getChat(actor.getName(), chatId)
                .map(payload -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body((Payload) payload))
                .onErrorResume(ApplicationError.class, error ->
                        Mono.just(ResponseEntity.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(new ErrorPayload(error.getMessage()))));
    }
}
