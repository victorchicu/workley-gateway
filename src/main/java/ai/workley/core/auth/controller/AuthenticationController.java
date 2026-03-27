package ai.workley.core.auth.controller;

import ai.workley.core.auth.config.AuthenticationCookieProperties;
import ai.workley.core.auth.config.AuthenticatedJwtWebFilter;
import ai.workley.core.auth.model.AuthenticationError;
import ai.workley.core.auth.model.AuthenticationRequest.*;
import ai.workley.core.auth.model.AuthenticationResponse;
import ai.workley.core.auth.model.AuthenticationResponse.MeResponse;
import ai.workley.core.auth.service.AuthenticationService;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AuthenticationCookieProperties cookieProperties;

    public AuthenticationController(AuthenticationService authenticationService, AuthenticationCookieProperties cookieProperties) {
        this.authenticationService = authenticationService;
        this.cookieProperties = cookieProperties;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<AuthenticationResponse>> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth ->
                        auth != null && auth.isAuthenticated()
                                && auth.getPrincipal() instanceof AuthenticatedJwtWebFilter.AuthenticatedPrincipal
                )
                .<ResponseEntity<AuthenticationResponse>>map(auth -> {
                    AuthenticatedJwtWebFilter.AuthenticatedPrincipal principal
                            = (AuthenticatedJwtWebFilter.AuthenticatedPrincipal) auth.getPrincipal();
                    return ResponseEntity.ok(new MeResponse(principal.email()));
                })
                .defaultIfEmpty(ResponseEntity.status(401).build());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthenticationResponse>> login(@RequestBody LoginRequest request) {
        return authenticationService.login(request.email(), request.password())
                .<ResponseEntity<AuthenticationResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthenticationError.class, this::handleError);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<AuthenticationResponse>> logout(ServerHttpRequest request, ServerHttpResponse response) {
        String refreshToken = Optional.ofNullable(
                        request.getCookies().getFirst(cookieProperties.refreshTokenCookieName()))
                .map(HttpCookie::getValue)
                .orElse(null);
        return authenticationService.logout(refreshToken, response)
                .<ResponseEntity<AuthenticationResponse>>thenReturn(ResponseEntity.ok().build())
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().build()));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthenticationResponse>> register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request.email(), request.password(), request.passwordConfirmation())
                .<ResponseEntity<AuthenticationResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthenticationError.class, this::handleError);
    }

    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<AuthenticationResponse>> verifyOtp(@RequestBody VerifyOtpRequest request, ServerHttpResponse response) {
        return authenticationService.verifyOtp(request.preAuthToken(), request.otp(), response)
                .<ResponseEntity<AuthenticationResponse>>thenReturn(ResponseEntity.ok().build())
                .onErrorResume(AuthenticationError.class, this::handleError);
    }

    @PostMapping("/continue")
    public Mono<ResponseEntity<AuthenticationResponse>> continueAuth(@RequestBody ContinueRequest request) {
        return authenticationService.continueAuth(request.email())
                .<ResponseEntity<AuthenticationResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthenticationError.class, this::handleError);
    }


    private Mono<ResponseEntity<AuthenticationResponse>> handleError(AuthenticationError error) {
        return Mono.just(ResponseEntity.status(error.getStatus())
                .body(new AuthenticationResponse.AuthenticationErrorResponse(error.getErrorCode(), error.getMessage())));
    }
}
