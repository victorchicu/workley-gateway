package app.awaytogo.gateway.shared;

import app.awaytogo.gateway.resume.api.dto.ErrorResponseDto;
import app.awaytogo.gateway.resume.api.exception.ResumeNotFoundApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResumeNotFoundApiException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponseDto> handleResumeNotFound(ResumeNotFoundApiException ex) {
        log.error("Resume not found: {}", ex.getMessage());
        return Mono.just(ErrorResponseDto.builder()
                .errorCode("RESUME_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }
}
