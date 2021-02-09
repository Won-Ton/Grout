package com.github.wonton.grout.inject;

import com.github.wonton.grout.Utils;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is a tool for merging child elements of a registry entry that ought to have been registry entries themselves.
 * The merge operator should be additive only to ensure DataPacks function authoritatively over mods.
 */
public class RegistryEntryInjector<E> {

    private static final Map<RegistryKey<?>, RegistryEntryInjector<?>> INJECTORS = new ConcurrentHashMap<>();

    // The registry entry codec.
    private final Codec<E> entryCodec;

    // Merges child elements into the registry entry json.
    private final BinaryOperator<JsonElement> merger;

    // Maps the child elements already held in the registry entry json to a stream of 'child' jsons.
    private final Function<JsonElement, Stream<JsonElement>> mapper;

    // Contains lists of child elements to merge into specific registry entry jsons.
    private final Map<RegistryKey<E>, Collection<JsonElement>> children = new IdentityHashMap<>();

    public RegistryEntryInjector(RegistryKey<Registry<E>> registryKey, Codec<E> elementCodec, BinaryOperator<JsonElement> merger, Function<JsonElement, Stream<JsonElement>> mapper) {
        this.entryCodec = elementCodec;
        this.merger = Utils.wrap(merger);
        this.mapper = Utils.wrap(mapper);
        RegistryEntryInjector.INJECTORS.put(registryKey, this);
    }

    boolean hasChildren(RegistryKey<?> key) {
        return !children.getOrDefault(key, Collections.emptyList()).isEmpty();
    }

    BinaryOperator<JsonElement> getMerger() {
        return merger;
    }

    Function<JsonElement, Stream<JsonElement>> getMapper() {
        return mapper;
    }

    public void registerAll(Registry<E> registry, Predicate<JsonElement> filter) {
        children.clear();
        for (Map.Entry<RegistryKey<E>, E> entry : registry.getEntries()) {
            final RegistryKey<E> key = entry.getKey();
            final E value = entry.getValue();
            Utils.encode(value, entryCodec, json -> mapper.apply(json).filter(filter).forEach(child -> addChildEntry(key, child)));
        }
    }

    // Wording... sorry!
    public JsonElement injectChildren(RegistryKey<?> entryKey, JsonElement entryJson) {
        Collection<JsonElement> values = children.get(entryKey);
        if (values == null) {
            return entryJson;
        }
        return merge(entryJson, values, merger);
    }

    private void addChildEntry(RegistryKey<E> key, JsonElement entry) {
        children.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
    }

    private static JsonElement merge(final JsonElement root, Collection<JsonElement> elements, BinaryOperator<JsonElement> mergeFunc) {
        JsonElement result = root;
        for (JsonElement element : elements) {
            JsonElement merged = mergeFunc.apply(result, element);
            if (!merged.isJsonNull()) {
                result = merged;
            }
        }
        return result;
    }

    @Nullable
    public static RegistryEntryInjector<?> getInjector(RegistryKey<? extends Registry<?>> key) {
        return INJECTORS.get(key);
    }
}
