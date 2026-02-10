package io.github.khazubaidi.objects;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OneTimeTokeMetadata implements Serializable {

    private String name;
    private Object id;
}
