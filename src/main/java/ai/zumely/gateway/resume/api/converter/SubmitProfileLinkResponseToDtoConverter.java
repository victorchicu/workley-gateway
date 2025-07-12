package ai.zumely.gateway.resume.api.converter;

import ai.zumely.gateway.resume.api.dto.SubmitProfileLinkResponseDto;
import ai.zumely.gateway.resume.domain.command.impl.SubmitProfileLinkResponse;
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
