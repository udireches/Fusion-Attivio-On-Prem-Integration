package com.lucidworks.connector.plugin.cloudsupport.util;

public class MiscUtils {
    public static String loggerName(Class<?> clazz, String logContextName) {
        return clazz + (logContextName != null ? ("." + logContextName) : "");
    }
}
