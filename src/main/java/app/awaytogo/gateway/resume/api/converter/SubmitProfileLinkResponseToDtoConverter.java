package app.awaytogo.gateway.resume.api.converter;

import app.awaytogo.gateway.resume.api.dto.SubmitProfileLinkResponseDto;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitProfileLinkResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SubmitProfileLinkResponseToDtoConverter implements Converter<SubmitProfileLinkResponse, SubmitProfileLinkResponseDto> {

    @Override
    public SubmitProfileLinkResponseDto convert(SubmitProfileLinkResponse source) {
        return SubmitProfileLinkResponseDto
                .builder()

                .build();
    }
}
