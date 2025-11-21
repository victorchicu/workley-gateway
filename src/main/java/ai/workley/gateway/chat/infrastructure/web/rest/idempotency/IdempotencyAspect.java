package ai.workley.gateway.chat.infrastructure.web.rest.idempotency;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class IdempotencyAspect {
    @Around("@annotation(IdempotencyKey)")
    public Object injectIdempotencyKey(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof Mono<?> proceeding) {
            return Mono.deferContextual(contextView -> {
                String idempotencyKey = IdempotencyKeyContext.get(contextView);
                return proceeding.contextWrite(context -> IdempotencyKeyContext.save(context, idempotencyKey));
            });
        }
        return result;
    }
}