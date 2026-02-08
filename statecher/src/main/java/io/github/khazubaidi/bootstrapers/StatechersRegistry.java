package io.github.khazubaidi.bootstrapers;


import io.github.khazubaidi.exceptions.StatecherNotFoundException;
import io.github.khazubaidi.models.Statecher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

public class StatechersRegistry {

    private static final Logger log = LoggerFactory.getLogger(StatechersRegistry.class);

    private final Map<String, Statecher> statechers = new HashMap<>();

    public void register(String name, Statecher statecher) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Statecher name cannot be null or empty");
        }

        if (statecher == null) {
            throw new IllegalArgumentException("Statecher cannot be null");
        }

        statechers.put(name, statecher);
        log.info("Registered statecher: {}", name);
    }

    public Statecher get(String name) {

        Statecher statecher = statechers.get(name);

        if (statecher == null) {
            throw new StatecherNotFoundException(name);
        }

        return statecher;
    }

    public Statecher getOrNull(String name) {
        return statechers.get(name);
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
