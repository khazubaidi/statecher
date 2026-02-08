package io.github.khazubaidi.configurations;

import java.util.List;

public interface PermissionValidator {

    boolean has(List<String> permission);
    boolean hasAny(List<String> permission);
}
