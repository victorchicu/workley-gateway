package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.IntentType;
import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
public class ReplyContentBuilderRegistry {
    private final EnumMap<IntentType, ReplyContentBuilder<? extends Content>> builders = new EnumMap<>(IntentType.class);

    public ReplyContentBuilderRegistry(List<ReplyContentBuilder<? extends Content>> builders) {
        for (ReplyContentBuilder<? extends Content> builder : builders) {
            for (IntentType intentType : IntentType.values()) {
                if (builder.supports(intentType)) {
                    this.builders.put(intentType, builder);
                }
            }
        }
    }

    public ReplyContentBuilder<? extends Content> get(IntentType intent) {
        ReplyContentBuilder<? extends Content> builder = builders.get(intent);
        if (builder == null) {
            throw new IllegalArgumentException("No builder registered for intent: " + intent);
        }
        return builder;
    }
}
