package com.github.wonton.grout;

import com.github.wonton.grout.injector.RegistryEntryInjector;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public final class GroutResourceAccess implements WorldSettingsImport.IResourceAccess {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final GroutConfig config;
    private final IResourceManager resourceManager;
    private final Map<RegistryKey<? extends Registry<?>>, RegistryEntryInjector<?>> injectors;

    GroutResourceAccess(IResourceManager resourceManager, GroutConfig config, Map<RegistryKey<? extends Registry<?>>, RegistryEntryInjector<?>> injectors) {
        this.resourceManager = resourceManager;
        this.injectors = injectors;
        this.config = config;
    }

    @Override
    public String toString() {
        return "GroutResourceAccess[" + resourceManager + "]";
    }

    @Override
    public Collection<ResourceLocation> getRegistryObjects(RegistryKey<? extends Registry<?>> registryKey) {
        return resourceManager.getAllResourceLocations(registryKey.getLocation().getPath(), file -> file.endsWith(".json"));
    }

    @Override
    public <E> DataResult<Pair<E, OptionalInt>> decode(DynamicOps<JsonElement> ops, RegistryKey<? extends Registry<E>> registryKey, RegistryKey<E> entryKey, Decoder<E> decoder) {
        final ResourceLocation file = toJsonPath(registryKey, entryKey.getLocation());
        final RegistryEntryInjector<?> injector = injectors.get(registryKey);

        try {
            final JsonElement result = loadJson(entryKey, file, injector);

            if (result != null) {
                result.getAsJsonObject().addProperty("forge:registry_name", entryKey.getLocation().toString());
            }

            return decoder.parse(ops, result).map((instance) -> Pair.of(instance, OptionalInt.empty()));
        } catch (IOException e) {
            return DataResult.error("Failed to parse " + file + " file: " + e.getMessage());
        }
    }

    private JsonElement loadJson(RegistryKey<?> entryKey, ResourceLocation file, @Nullable RegistryEntryInjector<?> injector) throws IOException {
        if (injector == null || !config.hasInjections()) {
            return loadTop(file);
        }

        final JsonElement result;
        if (config.mergeDataPacks) {
            result = loadAll(entryKey, file, injector);
        } else {
            result = loadTop(file);
        }

        if (config.injectDefaults && result != null && result.isJsonObject() && injector.hasInjections()) {
            Grout.LOG.debug("Merging registry defaults for: {}", entryKey);
            injector.injectRaw(entryKey, result.getAsJsonObject());
        }

        return result;
    }

    private JsonElement loadTop(ResourceLocation file) throws IOException {
        // Vanilla behaviour: load the entry from the top-most DataPack that contains data for it.
        try (IResource resource = resourceManager.getResource(file); Reader reader = newReader(resource)) {
            return JSON_PARSER.parse(reader);
        }
    }

    private JsonElement loadAll(RegistryKey<?> entryKey, ResourceLocation file, RegistryEntryInjector<?> injector) throws IOException {
        // Merging behaviour: load the entry json from each DataPack that contains data for it.
        List<IResource> resources = resourceManager.getAllResources(file);
        if (resources.size() > 1) {
            Grout.LOG.debug("Merging datapack entries for: {}", entryKey);
        }

        JsonElement result = null;
        // Iterate resources in reverse order adding missing data without overwriting existing.
        for (int index = resources.size() - 1; index >= 0; index--) {
            try (IResource resource = resources.get(index); Reader reader = newReader(resource)) {
                JsonElement json = JSON_PARSER.parse(reader);

                if (result == null) {
                    // Nothing to merge if first value!
                    result = json;
                } else if (result.isJsonObject() && json.isJsonObject()) {
                    injector.merge(result.getAsJsonObject(), json.getAsJsonObject());
                }
            }
        }

        return result;
    }

    private static Reader newReader(IResource resource) {
        return new InputStreamReader(new BufferedInputStream(resource.getInputStream()), StandardCharsets.UTF_8);
    }

    private static ResourceLocation toJsonPath(RegistryKey<?> registryKey, ResourceLocation name) {
        return new ResourceLocation(
                name.getNamespace(),
                registryKey.getLocation().getPath() + "/" + name.getPath() + ".json"
        );
    }
}
