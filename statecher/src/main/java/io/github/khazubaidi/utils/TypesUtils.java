package io.github.khazubaidi.utils;

import io.github.khazubaidi.markers.StatecherRepository;
import org.springframework.core.GenericTypeResolver;

public class TypesUtils {

    private TypesUtils(){}

    public static Class<?> getRepositoryIdType(Class<?> repositoryClass) {

        Class<?>[] types = GenericTypeResolver.resolveTypeArguments(
                repositoryClass,
                StatecherRepository.class);

        return types != null ? types[1] : null;
    }

    public static Object convertId(Object id, Class<?> targetType) {

        if (targetType.equals(Long.class))
            return Long.valueOf(id.toString());


        if (targetType.equals(Integer.class))
            return Integer.valueOf(id.toString());


        throw new IllegalArgumentException("Unsupported ID type: " + targetType);
    }
}
