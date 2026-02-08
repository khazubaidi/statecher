package io.github.khazubaidi.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statecher")
public class StatecherProperties {

    public static final String SCHEMA_LOCATION = "classpath:statechers.schema.json";
    public static final String SCHEMA_FILENAME = "statecher.schema.json";
    public static final String STATECHERS_FILE_TYPE = "json";

    private String path = "classpath:/statechers/";
    private boolean enabled = true;

    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
