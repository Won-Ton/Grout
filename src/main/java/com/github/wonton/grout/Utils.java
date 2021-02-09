package com.github.wonton.grout;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Utils {

    public static <E> void encode(E entry, Codec<E> codec, Consumer<JsonElement> consumer) {
        codec.encodeStart(JsonOps.INSTANCE, entry).result().ifPresent(consumer);
    }

    public static JsonElement get(JsonElement owner, String key, String type) {
        Preconditions.checkState(owner.isJsonObject(), "Parent is not a JsonObject!");
        JsonElement result = owner.getAsJsonObject().get(key);
        Objects.requireNonNull(result, type);
        return result;
    }

    public static JsonObject getObject(JsonElement owner, String key, String type) {
        JsonElement result = get(owner, key, type);
        Preconditions.checkState(result.isJsonObject(), "%s is not a JsonObject!", type);
        return result.getAsJsonObject();
    }

    public static BinaryOperator<JsonElement> wrap(BinaryOperator<JsonElement> operator) {
        return (left, right) -> {
            try {
                return operator.apply(left, right);
            } catch (Throwable t) {
                t.printStackTrace();
                return left;
            }
        };
    }

    public static Function<JsonElement, Stream<JsonElement>> wrap(Function<JsonElement, Stream<JsonElement>> function) {
        return element -> {
            try {
                return function.apply(element);
            } catch (Throwable t) {
                t.printStackTrace();
                return Stream.empty();
            }
        };
    }
}
