package io.github.khazubaidi.utils;

import java.util.Objects;

public class DataUtils {

    private DataUtils(){}

    public static String toStringOrNull(Object val){

        if(Objects.isNull(val))
            return null;

        return val.toString();
    }
}
