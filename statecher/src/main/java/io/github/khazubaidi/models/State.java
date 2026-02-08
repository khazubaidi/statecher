package io.github.khazubaidi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class State {

    private List<String> validators = new ArrayList<>();
    private List<String> before = new ArrayList<>();
    private List<String> after = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<String> transitions = new ArrayList<>();
}
