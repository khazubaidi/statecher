package io.github.khazubaidi.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.configurations.IdGenerator;
import io.github.khazubaidi.configurations.PermissionValidator;
import io.github.khazubaidi.service.StatecherInitiateService;
import io.github.khazubaidi.service.StatecherInitiateServiceImpl;
import io.github.khazubaidi.service.StatecherProcessService;
import io.github.khazubaidi.service.StatecherProcessServiceImpl;
import io.github.khazubaidi.utils.BeanUtils;
import io.github.khazubaidi.utils.BeanUtilsImpl;
import io.github.khazubaidi.validations.JsonSchemaValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnProperty(prefix = "statechers", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(StatecherProperties.class)
@Import(StatechersBeanPostProcessor.class)
public class StatechersAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public JsonSchemaValidator jsonSchemaValidator(ObjectMapper objectMapper,
                                                   ResourcePatternResolver resourceResolver) {

        Resource schemaResource = resourceResolver.getResource(StatecherProperties.SCHEMA_LOCATION);
        return new JsonSchemaValidator(objectMapper, schemaResource);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatecherRegistry statechersRegistry() {

        return new StatecherRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanReferenceValidator beanReferenceValidator(
            ApplicationContext applicationContext,
            EntityManagerFactory entityManagerFactory) {

        return new BeanReferenceValidator(
                applicationContext,
                entityManagerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatecherInitiateService statecherInitiateService(
            IdGenerator idGenerator,
            StatecherRegistry statecherRegistry,
            PermissionValidator permissionValidator,
            BeanUtils beanUtils,
            EntityManagerFactory entityManagerFactory) {

        return new StatecherInitiateServiceImpl(
                idGenerator,
                statecherRegistry,
                permissionValidator,
                beanUtils,
                entityManagerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatecherProcessService statecherProcessService(
            StatecherRegistry statecherRegistry,
            PermissionValidator permissionValidator,
            BeanUtils beanUtils,
            EntityManagerFactory entityManagerFactory) {

        return new StatecherProcessServiceImpl(
                statecherRegistry,
                permissionValidator,
                beanUtils,
                entityManagerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanUtils BeanUtils(
            ApplicationContext applicationContext) {

        return new BeanUtilsImpl(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatecherLoader statechersLoader(ResourcePatternResolver resourceResolver,
                                            ObjectMapper objectMapper,
                                            JsonSchemaValidator schemaValidator,
                                            StatecherRegistry statecherRegistry,
                                            StatecherProperties properties,
                                            BeanReferenceValidator beanReferenceValidator) {

        return new StatecherLoader(
                resourceResolver,
                objectMapper,
                schemaValidator,
                statecherRegistry,
                properties,
                beanReferenceValidator);
    }

//    @Bean
//    public StatechersConfiguration statechersConfiguration(StatechersLoader statechersLoader,
//                                                           BeanReferenceValidator beanReferenceValidator,
//                                                           StatechersRegistry statechersRegistry) {
//
//        return new StatechersConfiguration(
//                statechersLoader,
//                beanReferenceValidator,
//                statechersRegistry);
//    }
}
