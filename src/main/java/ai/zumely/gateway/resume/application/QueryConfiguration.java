package ai.zumely.gateway.resume.application;

import ai.zumely.gateway.resume.domain.model.ReadModel;
import ai.zumely.gateway.resume.domain.query.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class QueryConfiguration {

    private final List<QueryHandler<? extends Query, ? extends ReadModel>> queryHandlers;

    public QueryConfiguration(List<QueryHandler<? extends Query, ?>> queryHandlers) {
        this.queryHandlers = queryHandlers;
    }

    @Bean
    public Map<String, QueryHandler<? extends Query, ? extends ReadModel>> queryHandlers() {
        return queryHandlers.stream()
                .collect(Collectors.toMap(
                        handler -> {
                            Type[] interfaces = handler.getClass().getGenericInterfaces();
                            for (Type type : interfaces) {
                                if (type instanceof ParameterizedType pt) {
                                    if (pt.getRawType().equals(QueryHandler.class)) {
                                        Type actualType = pt.getActualTypeArguments()[0];
                                        return ((Class<?>) actualType).getSimpleName();
                                    }
                                }
                            }
                            throw new IllegalStateException("Cannot determine query handler: " + handler.getClass());
                        },
                        Function.identity()
                ));
    }
}
