package ai.workley.gateway.chat.application.reply.processing;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public interface PromptBuilder {

    <T extends Content> Prompt build(Message<T> prompt, List<Message<? extends Content>> history);
}