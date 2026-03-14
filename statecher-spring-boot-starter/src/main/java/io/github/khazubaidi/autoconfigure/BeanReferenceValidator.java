package io.github.khazubaidi.autoconfigure;


import io.github.khazubaidi.exceptions.StatecherValidationException;
import io.github.khazubaidi.extendables.FormProcessor;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.models.Transition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanReferenceValidator {

    private static final Logger log = LoggerFactory.getLogger(BeanReferenceValidator.class);

    private final ApplicationContext applicationContext;
    private final EntityManagerFactory entityManagerFactory;;

    public BeanReferenceValidator(
            ApplicationContext applicationContext,
            EntityManagerFactory entityManagerFactory){

        this.applicationContext = applicationContext;
        this.entityManagerFactory = entityManagerFactory;

    }

    public void validate(Statecher statecher) {

        validateEntities(statecher.getEntity());
        validateForms(statecher.getTransitions());
    }

    private void validateForms(Map<String, Transition> states) {

        Set<String> formProcessors = states.values()
                .stream()
                .map(t -> t.getForm().getProcessor())
                .collect(Collectors.toSet());
        validateOfType(formProcessors, FormProcessor.class);
    }

    private void validateEntities(String entityClass)  {

        if(StringUtils.isBlank(entityClass))
            throw new StatecherValidationException("Repository is required");

        try {

            boolean doseEntityExists = entityManagerFactory.getMetamodel()
                    .getEntities()
                    .stream()
                    .anyMatch(e -> e.getJavaType().getName().equals(entityClass));

            if(!doseEntityExists)
                throw new ClassNotFoundException("Entity " + entityClass + " not found.");

        } catch (ClassNotFoundException e) {

            throw new StatecherValidationException("Bean " + entityClass + " not found.");
        }
    }

    private <T> void validateOfType(Set<String> references, Class<T> type) {

        if(references == null || references.isEmpty())
            return;

        for (String reference : references) {

            if(StringUtils.isBlank(reference))
                continue;

            try {

                Class<?> clazz = Class.forName(reference.trim());
                applicationContext.getBeansOfType(clazz);

                log.debug("Service bean found: {}", reference);
            } catch (NoSuchBeanDefinitionException | ClassNotFoundException e) {

                throw new StatecherValidationException("Bean " + reference + " must implement " + type.getName());
            }
        }
    }
}
