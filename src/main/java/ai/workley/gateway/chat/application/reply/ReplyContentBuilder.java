package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.domain.IntentType;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.intent.IntentClassification;

import java.util.List;

public interface ReplyContentBuilder<T extends Content> {

    boolean supports(IntentType intent);

    T build(String text, IntentClassification classification, List<Message<? extends Content>> history);
}