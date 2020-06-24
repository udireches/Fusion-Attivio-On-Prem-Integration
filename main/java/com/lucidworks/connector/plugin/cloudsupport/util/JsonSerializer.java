package com.lucidworks.connector.plugin.cloudsupport.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.text.SimpleDateFormat;

/**
 * Static utility class for getting an {@link ObjectMapper} that will serialize attivio sdk objects
 * correctly.
 */
public class JsonSerializer {
    private static final String XML_GREGORIAN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Module that contains all registered serializers and mixins.
     */
    private static final SimpleModule module = new SimpleModule("Attivio", Version.unknownVersion());

    /**
     * Create a new {@link ObjectMapper} for serializating data.
     */
    public static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        configureObjectMapper(mapper);
        return mapper;
    }

    public static void configureObjectMapper(ObjectMapper mapper) {
        mapper.setDateFormat(new SimpleDateFormat(XML_GREGORIAN));
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.registerModule(module);
    }

    /**
     * Indicate that <code>type</code> should use toString() to serialize data to json.
     */
    public static void serializeToString(Class<?> type) {
        addSerializer(type, ToStringSerializer.instance);
    }

    /**
     * Add a serializer for <code>type</code>.
     */
    public static <T> void addSerializer(
            Class<? extends T> type, com.fasterxml.jackson.databind.JsonSerializer<T> ser) {
        module.addSerializer(type, ser);
    }

    /**
     * Add a deserializer for <code>type</code>.
     */
    public static <T> void addDeserializer(
            Class<T> type, com.fasterxml.jackson.databind.JsonDeserializer<? extends T> des) {
        module.addDeserializer(type, des);
    }

    /**
     * Add a <code>mixIn</code> interface/class for a sdk <code>type</code>.
     */
    public static void addMixIn(Class<?> type, Class<?> mixIn) {
        module.setMixInAnnotation(type, mixIn);
    }
}
