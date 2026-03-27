package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Content;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public interface PromptBuilder {

    <T extends Content> Prompt build(Message<T> prompt, List<Message<? extends Content>> history);
}
