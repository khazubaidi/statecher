package io.github.khazubaidi.objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OneTimeTokeMetadata {

    private String name;
    private Object id;
}
