package ai.workley.gateway.chat.application.reply.builders;

import ai.workley.gateway.chat.application.reply.ReplyContentBuilder;
import ai.workley.gateway.chat.domain.IntentType;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.FindJobContent;
import ai.workley.gateway.chat.domain.intent.IntentClassification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FindJobReplyContentBuilder implements ReplyContentBuilder<FindJobContent> {

    @Override
    public boolean supports(IntentType intent) {
        return intent == IntentType.FIND_JOB;
    }

    @Override
    public FindJobContent build(String text, IntentClassification classification, List<Message<? extends Content>> history) {
        return new FindJobContent(text);
    }
}