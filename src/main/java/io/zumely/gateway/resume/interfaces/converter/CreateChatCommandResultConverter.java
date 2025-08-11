package io.zumely.gateway.resume.interfaces.converter;

import io.zumely.gateway.resume.application.command.data.CreateChatCommandResult;
import io.zumely.gateway.resume.application.event.CreateChatApplicationEvent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateChatCommandResultConverter
        implements Converter<CreateChatApplicationEvent, CreateChatCommandResult> {
    @Override
    public CreateChatCommandResult convert(CreateChatApplicationEvent source) {
        return CreateChatCommandResult.response(source.chatId(), source.prompt());
    }
}
