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
public class Transition {

    private Object value;
    private String id;
    private Map<String, Object> extras;
}
