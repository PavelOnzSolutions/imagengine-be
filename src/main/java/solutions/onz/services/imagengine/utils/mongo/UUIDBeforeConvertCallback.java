package solutions.onz.services.imagengine.utils.mongo;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.UUID;

@Slf4j
@Component
public class UUIDBeforeConvertCallback implements BeforeConvertCallback<Object> {
    @NotNull
    @Override
    public Object onBeforeConvert(Object entity, @NotNull String collection) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.getType().equals(UUID.class)) {
                field.setAccessible(true);
                try {
                    if (null == field.get(entity)) {
                        field.set(entity, UUID.randomUUID());
                    }
                } catch (IllegalAccessException e) {
                    log.error("Failed to preprocess UUID for Id field: {} : ", e.getMessage(), e.getCause());
                }
            }
        }
        return entity;
    }
}
