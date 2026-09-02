package io.github.khazubaidi.validations;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonSchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(JsonSchemaValidator.class);

    private final ObjectMapper objectMapper;
    private final Schema schema;

    public JsonSchemaValidator(@Autowired  ObjectMapper objectMapper,
                               @Value("classpath:statechers.schema.json") Resource schemaResource) {

        this.objectMapper = objectMapper;

        try (InputStream schemaInput = schemaResource.getInputStream()) {

            SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
            this.schema = schemaRegistry.getSchema(schemaInput);
        } catch (Exception e) {

            log.error("Failed to load JSON schema", e);
            throw new RuntimeException("Failed to load JSON schema", e);
        }
    }

    public Set<String> validate(InputStream jsonInput) {

        try {

            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            return toMessages(schema.validate(jsonNode));
        } catch (Exception e) {

            log.error("Failed to parse JSON input", e);
            throw new RuntimeException("Failed to parse JSON input", e);
        }
    }

    public Set<String> validate(String jsonContent) {

        try {

            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            return toMessages(schema.validate(jsonNode));
        } catch (Exception e) {

            log.error("Failed to parse JSON content", e);
            throw new RuntimeException("Failed to parse JSON content", e);
        }
    }

    private Set<String> toMessages(List<Error> errors) {

        return errors.stream()
                .map(Error::getMessage)
                .collect(Collectors.toSet());
    }

    public boolean isValid(InputStream jsonInput) {

        return validate(jsonInput).isEmpty();
    }

    public boolean isValid(String jsonContent) {

        return validate(jsonContent).isEmpty();
    }

    public String formatErrors(Set<String> errors) {

        return String.join("; ", errors);
    }
}
