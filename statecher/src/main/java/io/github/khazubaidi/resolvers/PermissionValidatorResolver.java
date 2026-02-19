package io.github.khazubaidi.resolvers;

import java.util.List;

public interface PermissionValidatorResolver {

    boolean hasAny(List<String> permission);
}
