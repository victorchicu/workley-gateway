package app.awaytogo.gateway.resume.api.converter;

import app.awaytogo.gateway.resume.api.dto.SubmitLinkedInPublicProfileResponseDto;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SubmitProfileLinkResponseToDtoConverter
        implements Converter<SubmitLinkedInPublicProfileResponse, SubmitLinkedInPublicProfileResponseDto> {

    @Override
    public SubmitLinkedInPublicProfileResponseDto convert(SubmitLinkedInPublicProfileResponse source) {
        return SubmitLinkedInPublicProfileResponseDto
                .builder()
                .build();
    }
}
