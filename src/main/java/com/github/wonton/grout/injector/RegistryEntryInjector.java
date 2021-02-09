package com.github.wonton.grout.injector;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public final class RegistryEntryInjector<E> implements Injector<E> {

    private final RegistryKey<Registry<E>> registryKey;
    private final List<Injector<E>> injectors;

    private RegistryEntryInjector(Builder<E> builder) {
        this.registryKey = builder.registryKey;
        this.injectors = ImmutableList.copyOf(builder.injectors);
    }

    public boolean hasInjections() {
        return !injectors.isEmpty();
    }

    public void injectRaw(RegistryKey<?> key, JsonObject registryEntry) throws InjectionException {
        RegistryKey<E> typedKey = RegistryKey.getOrCreateKey(registryKey, key.getLocation());
        inject(typedKey, registryEntry);
    }

    @Override
    public void inject(RegistryKey<E> key, JsonObject registryEntry) throws InjectionException {
        for (Injector<E> injector : injectors) {
            injector.inject(key, registryEntry);
        }
    }

    @Override
    public void merge(JsonObject registryEntryDest, JsonObject registryEntrySrc) throws InjectionException {
        for (Injector<E> merger : injectors) {
            merger.merge(registryEntryDest, registryEntrySrc);
        }
    }

    @Override
    public String toString() {
        return "RegistryEntryInjector{" +
                "registryKey=" + registryKey +
                ", injectors=" + injectors +
                '}';
    }

    public static <E> Builder<E> builder(RegistryKey<Registry<E>> registryKey) {
        return new Builder<E>(registryKey);
    }

    public static class Builder<E> {

        private final RegistryKey<Registry<E>> registryKey;
        private final List<Injector<E>> injectors = new ArrayList<>();

        private Builder(RegistryKey<Registry<E>> registryKey) {
            this.registryKey = registryKey;
        }

        public Builder<E> add(Injector<E> injector) {
            injectors.add(injector);
            return this;
        }

        public RegistryEntryInjector<E> build() {
            return new RegistryEntryInjector<E>(this);
        }
    }
}
