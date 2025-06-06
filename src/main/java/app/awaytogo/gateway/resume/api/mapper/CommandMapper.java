package app.awaytogo.gateway.resume.api.mapper;

import app.awaytogo.gateway.resume.api.dto.CreateResumeRequest;
import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommandMapper {
    private static final Logger log = LoggerFactory.getLogger(CommandMapper.class);

    private static final Pattern PATTERN = Pattern.compile("linkedin\\.com/in/([^/]+)/?");

    private CommandMapper() {
    }

    public CreateResumeCommand toCreateResumeCommand(Principal principal, CreateResumeRequest request) {
        log.info("Extracting LinkedIn profile ID from request: {}", request);

        Matcher matcher = PATTERN.matcher(request.source());
        if (!matcher.find()) {
            throw new ApiException("This is not a LinkedIn profile page URL.");
        }

        String id = matcher.group(1);
        if (StringUtils.isBlank(id)) {
            throw new ApiException("We couldn't extract a LinkedIn profile ID from the provided URL.");
        }

        return CreateResumeCommand.builder()
                .resumeId(id)
                .source(request.source())
                .principal(principal)
                .timestamp(Instant.now()).build();
    }
}