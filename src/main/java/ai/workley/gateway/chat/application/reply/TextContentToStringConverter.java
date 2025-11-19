package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.content.TextContent;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TextContentToStringConverter implements Converter<TextContent, String> {

    @Override
    public @Nullable String convert(TextContent source) {
        return source.value();
    }
}
