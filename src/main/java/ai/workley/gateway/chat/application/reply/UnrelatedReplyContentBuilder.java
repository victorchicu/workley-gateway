package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.IntentType;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.intent.IntentClassification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnrelatedReplyContentBuilder implements ReplyContentBuilder<TextContent> {
    @Override
    public boolean supports(IntentType intent) {
        return intent == IntentType.UNRELATED;
    }

    @Override
    public TextContent build(String text, IntentClassification classification, List<Message<? extends Content>> history) {
        return new TextContent(text);
    }
}