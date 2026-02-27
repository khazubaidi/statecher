package io.github.khazubaidi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Transition {

    private String value;
    private String id;
    private List<String> onExist = new ArrayList<>();
    private List<String> onEnter = new ArrayList<>();
    private Map<String, Object> extras = new HashMap<>();
    private Form form = new Form();

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Form {

        private String processor;
        private Map<String, Object> extras = new HashMap<>();
    }
}
