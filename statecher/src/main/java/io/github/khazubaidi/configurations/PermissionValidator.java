package io.github.khazubaidi.configurations;

import java.util.List;

public interface PermissionValidator {

    boolean hasAny(List<String> permission);
}
