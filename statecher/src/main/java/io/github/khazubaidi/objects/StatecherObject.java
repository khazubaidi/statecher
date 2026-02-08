package io.github.khazubaidi.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class StatecherObject {

    private final String id;
    private final List<Transition> transitions;

    public StatecherObject(String id, List<io.github.khazubaidi.models.Transition> transitions){

        this.id = id;
        this.transitions = transitions.stream()
                .map(transition -> new Transition(transition.getId(), transition.getExtras()))
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    public static class Transition {

        private String id;
        private Map<String, Object> extras;
    }
}
