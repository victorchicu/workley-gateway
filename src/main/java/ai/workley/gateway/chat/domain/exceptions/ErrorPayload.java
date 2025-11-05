package ai.workley.gateway.chat.domain.exceptions;

import ai.workley.gateway.chat.domain.Payload;

public record ErrorPayload(String message) implements Payload {
}
