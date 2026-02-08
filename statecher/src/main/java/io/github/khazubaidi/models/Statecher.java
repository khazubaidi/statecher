package io.github.khazubaidi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Statecher {

    private String name;
    private String entity;
    private Map<String, Transition> transitions;
    private Map<String, State> states;
}
