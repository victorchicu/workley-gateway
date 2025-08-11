package io.zumely.gateway.resume.application.query.data;

import java.time.Instant;

public record Message<T>(String id, T content, String sender, Instant timestamp, String status) {
}
