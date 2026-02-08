package io.github.khazubaidi.autoconfigure;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.models.Statecher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatechersBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StatechersBeanPostProcessor.class);

    private final StatecherLoader statecherLoader;
    private final BeanReferenceValidator beanReferenceValidator;
    private final StatecherRegistry statecherRegistry;
    private boolean validated = false;

    public StatechersBeanPostProcessor(
            StatecherLoader statecherLoader,
            BeanReferenceValidator beanReferenceValidator,
            StatecherRegistry statecherRegistry) {

        this.statecherLoader = statecherLoader;
        this.beanReferenceValidator = beanReferenceValidator;
        this.statecherRegistry = statecherRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (!validated && bean instanceof StatecherLoader && statecherLoader != null) {

            try {

                validateAllBeanReferences();
                validated = true;
            } catch (Exception e) {

                log.error("Statecher validation bean references failed", e);
                throw new RuntimeException("Statecher validation bean references failed: " + e.getMessage(), e);
            }
        }

        return bean;
    }

    private void validateAllBeanReferences() {

        log.info("Validating statechers bean references");
        for (Statecher statecher : statecherRegistry.getAllStatechers()) {

            log.debug("Validating statecher: {}", statecher.getName());
            beanReferenceValidator.validate(statecher);
        }

        log.info("Statechers bean references validated successfully");
    }
}
