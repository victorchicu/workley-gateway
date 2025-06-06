package app.awaytogo.gateway.resume.application.configuration;

import app.awaytogo.gateway.resume.application.handler.CommandHandler;
import app.awaytogo.gateway.resume.domain.command.Command;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CommandHandlerConfiguration {
    private final List<CommandHandler<? extends Command>> handlers;

    public CommandHandlerConfiguration(List<CommandHandler<? extends Command>> handlers) {
        this.handlers = handlers;
    }


    @Bean
    public IdGenerator idGenerator() {
        return new SimpleIdGenerator();
    }

    @Bean
    public Map<String, CommandHandler<? extends Command>> commandHandlers() {
        return handlers.stream()
                .collect(Collectors.toMap(
                        handler -> {
                            Type[] interfaces = handler.getClass().getGenericInterfaces();
                            for (Type type : interfaces) {
                                if (type instanceof ParameterizedType pt) {
                                    if (pt.getRawType().equals(CommandHandler.class)) {
                                        Type actualType = pt.getActualTypeArguments()[0];
                                        return ((Class<?>) actualType).getSimpleName();
                                    }
                                }
                            }
                            throw new IllegalStateException("Cannot determine command type for handler: " + handler.getClass());
                        },
                        Function.identity()
                ));
    }
}