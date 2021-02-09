package com.github.wonton.grout.injector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Optional;

public final class InjectorUtils {

    private InjectorUtils() {

    }

    public static <T> Optional<JsonElement> encode(T value, Codec<T> codec) {
        return codec.encodeStart(JsonOps.INSTANCE, value).result();
    }

    public static JsonElement get(JsonObject object, String key) throws Injector.InjectionException {
        if (object.has(key)) {
            return object.get(key);
        }
        throw new Injector.InjectionException("Object does not contain value for key '%s'!", key);
    }

    public static JsonObject getObject(JsonObject object, String key) throws Injector.InjectionException {
        JsonElement element = get(object, key);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        throw new Injector.InjectionException("Value for key '%s' is not a json object!", key);
    }
}
