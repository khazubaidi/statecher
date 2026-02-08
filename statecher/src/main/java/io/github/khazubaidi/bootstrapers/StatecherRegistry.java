package io.github.khazubaidi.bootstrapers;


import io.github.khazubaidi.exceptions.StatecherNotFoundException;
import io.github.khazubaidi.models.Statecher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StatecherRegistry {

    private static final Logger log = LoggerFactory.getLogger(StatecherRegistry.class);

    private final Map<String, Statecher> statechers = new HashMap<>();

    public void register(String name, Statecher statecher) {

        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Statecher name cannot be null or empty");

        if (statecher == null)
            throw new IllegalArgumentException("Statecher cannot be null");

        statechers.put(name, statecher);
        log.info("Statecher ({}) was registered.", name);
    }

    public Statecher get(String name) {

        return Optional.ofNullable(statechers.get(name))
                .orElseThrow(() -> new StatecherNotFoundException(name));
    }

    public boolean exists(String name) {

        return statechers.containsKey(name);
    }

    public Set<String> getStatecherNames() {

        return Collections.unmodifiableSet(statechers.keySet());
    }

    public Collection<Statecher> getAllStatechers() {

        return Collections.unmodifiableCollection(statechers.values());
    }

    public int size() {

        return statechers.size();
    }
}
