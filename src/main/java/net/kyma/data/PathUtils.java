package net.kyma.data;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtils {
    static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}
