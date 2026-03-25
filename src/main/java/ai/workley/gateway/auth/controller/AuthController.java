package ai.workley.gateway.auth.controller;

import ai.workley.gateway.auth.config.AuthCookieProperties;
import ai.workley.gateway.auth.config.AuthenticatedJwtWebFilter;
import ai.workley.gateway.auth.model.AuthError;
import ai.workley.gateway.auth.model.AuthRequest.*;
import ai.workley.gateway.auth.model.AuthResponse;
import ai.workley.gateway.auth.model.AuthResponse.AuthErrorResponse;
import ai.workley.gateway.auth.model.AuthResponse.MeResponse;
import ai.workley.gateway.auth.service.AuthService;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieProperties cookieProperties;

    public AuthController(AuthService authService, AuthCookieProperties cookieProperties) {
        this.authService = authService;
        this.cookieProperties = cookieProperties;
    }

    @PostMapping("/continue")
    public Mono<ResponseEntity<AuthResponse>> continueAuth(@RequestBody ContinueRequest request) {
        return authService.continueAuth(request.email())
                .<ResponseEntity<AuthResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthError.class, this::handleError);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@RequestBody RegisterRequest request) {
        return authService.register(request.email(), request.password(), request.passwordConfirmation())
                .<ResponseEntity<AuthResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthError.class, this::handleError);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password())
                .<ResponseEntity<AuthResponse>>map(ResponseEntity::ok)
                .onErrorResume(AuthError.class, this::handleError);
    }

    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest request,
                                                         ServerHttpResponse response) {
        return authService.verifyOtp(request.preAuthToken(), request.otp(), response)
                .<ResponseEntity<AuthResponse>>thenReturn(ResponseEntity.ok().build())
                .onErrorResume(AuthError.class, this::handleError);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<AuthResponse>> logout(ServerHttpRequest request, ServerHttpResponse response) {
        String refreshToken = Optional.ofNullable(
                        request.getCookies().getFirst(cookieProperties.refreshTokenCookieName()))
                .map(HttpCookie::getValue)
                .orElse(null);
        return authService.logout(refreshToken, response)
                .<ResponseEntity<AuthResponse>>thenReturn(ResponseEntity.ok().build())
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().build()));
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<AuthResponse>> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth != null && auth.isAuthenticated()
                        && auth.getPrincipal() instanceof AuthenticatedJwtWebFilter.AuthenticatedPrincipal)
                .<ResponseEntity<AuthResponse>>map(auth -> {
                    var principal = (AuthenticatedJwtWebFilter.AuthenticatedPrincipal) auth.getPrincipal();
                    return ResponseEntity.ok(new MeResponse(principal.email()));
                })
                .defaultIfEmpty(ResponseEntity.status(401).build());
    }

    private Mono<ResponseEntity<AuthResponse>> handleError(AuthError error) {
        return Mono.just(ResponseEntity.status(error.getStatus())
                .body(new AuthErrorResponse(error.getErrorCode(), error.getMessage())));
    }
}
