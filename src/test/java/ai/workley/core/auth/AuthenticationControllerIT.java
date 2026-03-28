package ai.workley.core.auth;

import ai.workley.core.chat.TestRunner;
import ai.workley.core.auth.model.AuthenticationRequest.*;
import ai.workley.core.auth.model.AuthenticationResponse.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationControllerIT extends TestRunner {
    private static final String AUTH_URL = "/api/auth";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Test
    void continueWithNewEmail_shouldReturnRegister() {
        ContinueResponse response = webTestClient.post().uri(AUTH_URL + "/continue")
                .bodyValue(new ContinueRequest("newuser@example.com"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ContinueResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(response);
        assertEquals("register", response.nextStep());
    }

    @Test
    void continueWithInvalidEmail_shouldReturn400() {
        webTestClient.post().uri(AUTH_URL + "/continue")
                .bodyValue(new ContinueRequest("not-an-email"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void fullRegistrationFlow() {
        String email = "register-test@example.com";

        // Step 1: Continue
        ContinueResponse continueResp = webTestClient.post().uri(AUTH_URL + "/continue")
                .bodyValue(new ContinueRequest(email))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ContinueResponse.class)
                .returnResult().getResponseBody();

        assertEquals("register", continueResp.nextStep());

        // Step 2: Register
        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest(email, "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(registerResp);
        assertEquals("verify_otp", registerResp.nextStep());
        assertNotNull(registerResp.preAuthToken());

        // Step 3: Verify OTP
        webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("accessToken")
                .expectCookie().exists("refreshToken");
    }

    @Test
    void fullLoginFlow() {
        String email = "login-test@example.com";

        // Register first
        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest(email, "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "123456"))
                .exchange()
                .expectStatus().isOk();

        // Now continue should return login
        ContinueResponse continueResp = webTestClient.post().uri(AUTH_URL + "/continue")
                .bodyValue(new ContinueRequest(email))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ContinueResponse.class)
                .returnResult().getResponseBody();

        assertEquals("login", continueResp.nextStep());

        // Login
        StepResponse loginResp = webTestClient.post().uri(AUTH_URL + "/login")
                .bodyValue(new LoginRequest(email, "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        assertEquals("verify_otp", loginResp.nextStep());

        // Verify OTP
        webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(loginResp.preAuthToken(), "123456"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void wrongPassword_shouldReturn401() {
        String email = "wrong-pass@example.com";

        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest(email, "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "123456"))
                .exchange();

        webTestClient.post().uri(AUTH_URL + "/login")
                .bodyValue(new LoginRequest(email, "wrongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void wrongOtp_shouldReturn401() {
        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest("otp-test@example.com", "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "999999"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void meWithoutToken_shouldReturn401() {
        webTestClient.get().uri(AUTH_URL + "/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void meWithValidToken_shouldReturnEmail() {
        String email = "me-test@example.com";

        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest(email, "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        ResponseCookie accessCookie = webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "123456"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseCookies().getFirst("accessToken");

        assertNotNull(accessCookie);

        MeResponse meResp = webTestClient.get().uri(AUTH_URL + "/me")
                .cookie("accessToken", accessCookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody(MeResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(meResp);
        assertEquals(email, meResp.email());
    }

    @Test
    void logout_shouldClearCookies() {
        String email = "logout-test@example.com";

        StepResponse registerResp = webTestClient.post().uri(AUTH_URL + "/register")
                .bodyValue(new RegisterRequest(email, "password123", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(StepResponse.class)
                .returnResult().getResponseBody();

        var verifyResult = webTestClient.post().uri(AUTH_URL + "/verify-otp")
                .bodyValue(new VerifyOtpRequest(registerResp.preAuthToken(), "123456"))
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class);

        ResponseCookie refreshCookie =
                verifyResult.getResponseCookies().getFirst("refreshToken");
        assertNotNull(refreshCookie);

        webTestClient.post().uri(AUTH_URL + "/logout")
                .cookie("refreshToken", refreshCookie.getValue())
                .exchange()
                .expectStatus().isOk()
                .expectCookie().maxAge("accessToken", java.time.Duration.ZERO)
                .expectCookie().maxAge("refreshToken", java.time.Duration.ZERO);
    }
}
