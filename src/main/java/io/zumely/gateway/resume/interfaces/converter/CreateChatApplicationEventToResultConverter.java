package io.zumely.gateway.resume.interfaces.converter;

import io.zumely.gateway.resume.application.command.result.impl.CreateChatResult;
import io.zumely.gateway.resume.application.event.CreateChatApplicationEvent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateChatApplicationEventToResultConverter implements Converter<CreateChatApplicationEvent, CreateChatResult> {
    @Override
    public CreateChatResult convert(CreateChatApplicationEvent source) {
        return CreateChatResult.response(source.chatId(), source.prompt());
    }
}