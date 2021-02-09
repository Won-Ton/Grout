package com.github.wonton.grout.inject;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.codec.Codecs;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class RegistryEntryInjector<E, V> {

    private static final Map<RegistryKey<?>, RegistryEntryInjector<?, ?>> INJECTORS = new ConcurrentHashMap<>();

    private final RegistryKey<Registry<E>> registryKey;
    private final Codec<E> entryCodec;
    private final Codec<V> childCodec;
    private final BinaryOperator<JsonElement> entryMerger;
    private final Function<JsonElement, Stream<JsonElement>> entryMapper;

    private final Map<RegistryKey<E>, Collection<JsonElement>> children = new IdentityHashMap<>();

    public RegistryEntryInjector(RegistryKey<Registry<E>> registryKey,
                                 Codec<E> elementCodec, Codec<V> childCodec,
                                 BinaryOperator<JsonElement> entryMerger,
                                 Function<JsonElement, Stream<JsonElement>> entryMapper) {
        this.registryKey = registryKey;
        this.entryCodec = elementCodec;
        this.childCodec = childCodec;
        this.entryMerger = entryMerger;
        this.entryMapper = entryMapper;
        RegistryEntryInjector.INJECTORS.put(registryKey, this);
    }

    public void clear() {
        children.clear();
    }

    public BinaryOperator<JsonElement> getEntryMerger() {
        return entryMerger;
    }

    public Function<JsonElement, Stream<JsonElement>> getEntryMapper() {
        return entryMapper;
    }

    public void registerAll(Registry<E> registry) {
        for (Map.Entry<RegistryKey<E>, E> entry : registry.getEntries()) {
            registerOne(entry.getKey(), entry.getValue());
        }
    }

    public void registerOne(RegistryKey<E> key, E value) {
        Codecs.encode(value, entryCodec, json -> entryMapper.apply(json).forEach(entry -> register(key, entry)));
    }

    public void register(RegistryKey<E> key, V entry) {
        Codecs.encode(entry, childCodec, json -> register(key, json));
    }

    @SuppressWarnings("unchecked")
    public void injectUnchecked(SimpleRegistry<?> registry) {
        if (registry.getRegistryKey() == registryKey) {
            SimpleRegistry<E> typed = (SimpleRegistry<E>) registry;
            mergeEntries(typed);
        }
    }

    private void register(RegistryKey<E> key, JsonElement entry) {
        children.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
    }

    private void mergeEntries(SimpleRegistry<E> registry) {
        for (Map.Entry<RegistryKey<E>, Collection<JsonElement>> option : children.entrySet()) {
            E value = registry.getValueForKey(option.getKey());
            JsonElement root = entryCodec.encodeStart(JsonOps.INSTANCE, value).result().orElse(JsonNull.INSTANCE);

            if (root.isJsonNull()) {
                continue;
            }

            Grout.LOG.info("Visiting registry entry: {}", option.getKey());
            JsonElement result = merge(root, option.getValue(), entryMerger);
            Codecs.decode(result, entryCodec, newValue -> setEntry(registry, option.getKey(), newValue));
        }
    }

    private static <E> void setEntry(SimpleRegistry<E> registry, RegistryKey<E> key, E entry) {
        registry.validateAndRegister(OptionalInt.empty(), key, entry, Lifecycle.stable());
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

    public static void inject(SimpleRegistry<?> registry) {
        RegistryEntryInjector<?, ?> injector = getInjector(registry.getRegistryKey());
        if (injector != null) {
            injector.injectUnchecked(registry);
        }
    }

    @Nullable
    public static RegistryEntryInjector<?, ?> getInjector(RegistryKey<? extends Registry<?>> key) {
        return INJECTORS.get(key);
    }
}
