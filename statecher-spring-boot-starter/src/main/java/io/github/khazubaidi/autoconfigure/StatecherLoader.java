package io.github.khazubaidi.autoconfigure;

import io.github.khazubaidi.bootstrapers.StatecherRegistry;
import io.github.khazubaidi.models.Statecher;
import io.github.khazubaidi.validations.JsonSchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.util.Set;

public class StatecherLoader implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(StatecherLoader.class);

    private final ResourcePatternResolver resourceResolver;
    private final ObjectMapper objectMapper;
    private final JsonSchemaValidator schemaValidator;
    private final StatecherRegistry statecherRegistry;
    private final StatecherProperties properties;
    private final BeanReferenceValidator beanReferenceValidator;

    public StatecherLoader(
            ResourcePatternResolver resourceResolver,
            ObjectMapper objectMapper,
            JsonSchemaValidator schemaValidator,
            StatecherRegistry statecherRegistry,
            StatecherProperties properties,
            BeanReferenceValidator beanReferenceValidator) {

        this.resourceResolver = resourceResolver;
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
        this.statecherRegistry = statecherRegistry;
        this.properties = properties;
        this.beanReferenceValidator = beanReferenceValidator;
    }

@Override
    public void afterPropertiesSet() {

        if (!properties.isEnabled()) {

            log.info("Statecher is disabled");
            return;
        }

        loadStatechers();
    }

    public void loadStatechers() {

        String configLocation = properties.getPath();

        try {

            Resource[] resources = resourceResolver.getResources(configLocation + "*." + StatecherProperties.STATECHERS_FILE_TYPE);
            if (resources.length == 0) {

                log.info("No statechers configuration files found in {}", configLocation);
                return;
            }

            for (Resource resource : resources) {

                String filename = resource.getFilename();
                if (filename == null || filename.equals(StatecherProperties.SCHEMA_FILENAME))
                    return;

                String statecherName = filename.replace("." + StatecherProperties.STATECHERS_FILE_TYPE, "");
                loadStatecher(statecherName, resource);
            }

            log.info("Loaded {} statecher(s)", statecherRegistry.size());

        } catch (Exception e) {

            log.error("Failed to load statecher configurations", e);
            throw new RuntimeException("Failed to load statecher configurations", e);
        }
    }

    private void loadStatecher(String name, Resource resource) {

        try (InputStream inputStream = resource.getInputStream()) {

            Set<ValidationMessage> schemaErrors = schemaValidator.validate(inputStream);

            if (!schemaErrors.isEmpty()) {

                String errors = schemaValidator.formatErrors(schemaErrors);
                throw new RuntimeException("JSON schema validation failed for statecher '" + name + "': " + errors);
            }

        } catch (Exception e) {

            log.error("statecher: {} had failed to load.", name);
            throw new RuntimeException("statecher: " + name + " had failed to load", e);
        }

        try (InputStream inputStream = resource.getInputStream()) {

            Statecher statecher = objectMapper.readValue(inputStream, Statecher.class);
            beanReferenceValidator.validate(statecher);
            statecherRegistry.register(statecher.getName(), statecher);
            log.info("statecher: {} loaded successfully", name);
        } catch (Exception e) {

            log.error("statecher: {} had failed to parse.", name);
            throw new RuntimeException("statecher: " + name + " had failed to parse.", e);
        }
    }
}
