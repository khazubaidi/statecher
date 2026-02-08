package io.github.khazubaidi.validations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonSchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(JsonSchemaValidator.class);

    private final ObjectMapper objectMapper;
    private final JsonSchema schema;

    public JsonSchemaValidator(@Autowired  ObjectMapper objectMapper,
                               @Value("classpath:statechers.schema.json") Resource schemaResource) {

        this.objectMapper = objectMapper;

        try (InputStream schemaInput = schemaResource.getInputStream()) {

            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            this.schema = schemaFactory.getSchema(schemaInput);
        } catch (Exception e) {

            log.error("Failed to load JSON schema", e);
            throw new RuntimeException("Failed to load JSON schema", e);
        }
    }

    public Set<ValidationMessage> validate(InputStream jsonInput) {

        try {

            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            return schema.validate(jsonNode);
        } catch (Exception e) {

            log.error("Failed to parse JSON input", e);
            throw new RuntimeException("Failed to parse JSON input", e);
        }
    }

    public Set<ValidationMessage> validate(String jsonContent) {

        try {

            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            return schema.validate(jsonNode);
        } catch (Exception e) {

            log.error("Failed to parse JSON content", e);
            throw new RuntimeException("Failed to parse JSON content", e);
        }
    }

    public boolean isValid(InputStream jsonInput) {

        return validate(jsonInput).isEmpty();
    }

    public boolean isValid(String jsonContent) {

        return validate(jsonContent).isEmpty();
    }

    public String formatErrors(Set<ValidationMessage> errors) {

        return errors.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining("; "));
    }
}
