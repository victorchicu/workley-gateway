package app.awaytogo.gateway.resume.application;

import app.awaytogo.gateway.resume.domain.command.Command;
import app.awaytogo.gateway.resume.domain.command.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CommandConfiguration {

    private final List<CommandHandler<? extends Command, ? extends Response>> commandHandlers;

    public CommandConfiguration(
            List<CommandHandler<? extends Command, ? extends Response>> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }


    @Bean
    public Map<String, CommandHandler<? extends Command, ? extends Response>> commandHandlers() {
        return commandHandlers.stream()
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
                            throw new IllegalStateException("Cannot determine command handler: " + handler.getClass());
                        },
                        Function.identity()
                ));
    }
}