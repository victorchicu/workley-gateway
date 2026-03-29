package ai.workley.core.auth.service;

import ai.workley.core.auth.config.SendGridProperties;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class SendGridEmailService {
    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);

    private final String otpTemplate;
    private final SendGrid sendGrid;
    private final SendGridProperties properties;

    public SendGridEmailService(SendGridProperties properties) throws IOException {
        this.properties = properties;
        this.sendGrid = new SendGrid(properties.getApiKey());
        this.otpTemplate = new ClassPathResource("templates/otp-email.html").getContentAsString(StandardCharsets.UTF_8);
    }

    public Mono<Void> sendOtp(String toEmail, String code) {
        return Mono.fromCallable(() -> {
            String digitCells = buildDigitCells(code);
            String html = otpTemplate.replace("{{OTP_DIGITS}}", digitCells);

            Email from = new Email(properties.getFromEmail(), properties.getFromName());
            Email to = new Email(toEmail);
            Content content = new Content("text/html", html);
            Mail mail = new Mail(from, "Your verification code", to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " " + response.getBody());
            }
            log.info("OTP email sent to {} (status: {})", toEmail, response.getStatusCode());
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private String buildDigitCells(String code) {
        StringBuilder sb = new StringBuilder();
        for (char digit : code.toCharArray()) {
            sb.append("<td style=\"padding: 0 4px;\">")
              .append("<div style=\"width: 40px; height: 48px; line-height: 48px; text-align: center; ")
              .append("font-size: 22px; font-weight: 600; color: #0d0d0d; ")
              .append("background-color: #ffffff; border: 1px solid #00000033; border-radius: 12px;\">")
              .append(digit)
              .append("</div></td>");
        }
        return sb.toString();
    }
}
