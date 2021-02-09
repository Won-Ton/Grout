package com.github.wonton.grout.injector;

import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;

import java.io.IOException;

public interface Injector<E> {

    /**
     * Insert default data into the registry entry that isn't already present.
     *
     * @param key           The RegistryKey of the registry entry.
     * @param registryEntry The json data for the registry entry.
     * @throws InjectionException When invalid json data is passed into the inject function.
     */
    void inject(RegistryKey<E> key, JsonObject registryEntry) throws InjectionException;

    /**
     * Insert data from the 'src' registry entry that isn't already present in the 'dest' registry entry.
     *
     * @param registryEntryDest The registry entry to add missing data to.
     * @param registryEntrySrc  The registry entry to add missing data from.
     * @throws InjectionException When invalid json data is passed into the merge function.
     */
    void merge(JsonObject registryEntryDest, JsonObject registryEntrySrc) throws InjectionException;

    class InjectionException extends IOException {
        public InjectionException(String messageFormat, Object arg) {
            super(String.format(messageFormat, arg));
        }
    }
}
