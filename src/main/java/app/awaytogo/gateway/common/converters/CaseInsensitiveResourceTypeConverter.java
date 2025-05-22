package app.awaytogo.gateway.common.converters;

import app.awaytogo.gateway.common.types.ResourceType;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CaseInsensitiveResourceTypeConverter implements Converter<String, ResourceType> {

    @Override
    public ResourceType convert(@NotNull String source) {
        ResourceType resource = EnumUtils.getEnumIgnoreCase(ResourceType.class, source);
        return Objects.requireNonNull(resource, "Invalid resource: " + source);
    }
}
