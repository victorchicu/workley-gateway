package ai.workley.gateway.chat.application.reply.prompts;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DefaultPromptBuilder implements PromptBuilder {
    private static final Logger log = LoggerFactory.getLogger(DefaultPromptBuilder.class);

    private final ConversionService conversionService;

    public DefaultPromptBuilder(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public <T extends Content> Prompt build(Message<T> prompt, List<Message<? extends Content>> history) {
        List<org.springframework.ai.chat.messages.Message> list = new ArrayList<>();

        for (Message<? extends Content> message : history) {
            String text = extractText(message);
            switch (message.role()) {
                case ANONYMOUS, CUSTOMER -> list.add(new UserMessage(text));
                case ASSISTANT -> list.add(new AssistantMessage(text));
                default -> log.warn("Ignoring role in history: {}", message.role());
            }
        }

        boolean missingLast = history.isEmpty() ||
                !history.getLast().id().equals(prompt.id());

        if (missingLast) {
            list.add(new UserMessage(extractText(prompt)));
        }

        return new Prompt(list);
    }

    private <T extends Content> @NonNull String extractText(Message<T> prompt) {
        return Objects.requireNonNull(
                conversionService.convert(prompt.content(), String.class),
                "Can't convert content to string"
        );
    }
}