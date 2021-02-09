package com.github.wonton.grout.injector;

import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;

import java.io.IOException;

public interface Injector<E> {

    void inject(RegistryKey<E> key, JsonObject registryEntry) throws InjectionException;

    void merge(JsonObject registryEntryDest, JsonObject registryEntrySrc) throws InjectionException;

    class InjectionException extends IOException {
        public InjectionException(String messageFormat, Object arg) {
            super(String.format(messageFormat, arg));
        }
    }
}
