package io.github.khazubaidi.utils;

import io.github.khazubaidi.exceptions.StatecherException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeanUtilsImpl implements BeanUtils {

    private final ApplicationContext applicationContext;

    @Override
    public <T> T findByName(String name, Class<T> type) {

        try {

            Class<?> klazz = Class.forName(name);
            Object bean  = applicationContext.getBean(klazz);

            return type.cast(bean);
        } catch (ClassNotFoundException e) {

            throw new StatecherException("Bean (" + name + ") not found.", e.getCause());
        }
    }
}
