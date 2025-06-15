package app.awaytogo.gateway.resume.api.converter;

import app.awaytogo.gateway.resume.domain.command.impl.SubmitProfileLinkCommand;
import app.awaytogo.gateway.resume.api.dto.SubmitProfileLinkCommandDto;
import app.awaytogo.gateway.resume.api.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DtoToSubmitProfileLinkCommandConverter
        implements Converter<SubmitProfileLinkCommandDto, SubmitProfileLinkCommand> {
    private static final Pattern PATTERN = Pattern.compile("linkedin\\.com/in/([^/]+)/?");

    @Override
    public SubmitProfileLinkCommand convert(SubmitProfileLinkCommandDto source) {
        Matcher matcher = PATTERN.matcher(source.url());
        if (matcher.find()) {
            String id = matcher.group(1);
            if (StringUtils.isNotBlank(id)) {
                return SubmitProfileLinkCommand.builder()
                        .resumeId(id)
                        .build();
            }
        }
        throw new ApiException("We couldn't extract a LinkedIn profile ID from the provided URL.");
    }
}