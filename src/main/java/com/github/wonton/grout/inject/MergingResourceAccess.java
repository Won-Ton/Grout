package com.github.wonton.grout.inject;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.Injectors;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.OptionalInt;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class MergingResourceAccess implements WorldSettingsImport.IResourceAccess {

    private final IResourceManager manager;
    private final JsonParser parser = new JsonParser();

    public MergingResourceAccess(IResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "RegistryInjectorResourceAccess[" + manager + "]";
    }

    @Override
    public Collection<ResourceLocation> getRegistryObjects(RegistryKey<? extends Registry<?>> registryKey) {
        return manager.getAllResourceLocations(registryKey.getLocation().getPath(), file -> file.endsWith(".json"));
    }

    @Override
    public <E> DataResult<Pair<E, OptionalInt>> decode(DynamicOps<JsonElement> ops, RegistryKey<? extends Registry<E>> registryKey, RegistryKey<E> entryKey, Decoder<E> decoder) {
        final ResourceLocation entryName = entryKey.getLocation();
        final ResourceLocation entryPath = toJsonPath(registryKey, entryName);
        final RegistryEntryInjector<?> injector = RegistryEntryInjector.getInjector(registryKey);

        try {
            final JsonElement result = loadJson(entryKey, entryPath, injector);

            if (result != null) {
                result.getAsJsonObject().addProperty("forge:registry_name", entryKey.getLocation().toString());
            }

            return decoder.parse(ops, result).map((instance) -> Pair.of(instance, OptionalInt.empty()));
        } catch (IOException e) {
            return DataResult.error("Failed to parse " + entryPath + " file: " + e.getMessage());
        }
    }

    private JsonElement loadJson(RegistryKey<?> entryKey, ResourceLocation entryPath, @Nullable RegistryEntryInjector<?> injector) throws IOException {
        if (injector == null) {
            // No injector for this registry type so load the 'top' DataPack entry as normal.
            try (IResource resource = manager.getResource(entryPath); Reader reader = newReader(resource)) {
                return parser.parse(reader);
            }
        }
        return loadAndMerge(entryKey, entryPath, injector);
    }

    private JsonElement loadAndMerge(RegistryKey<?> entryKey, ResourceLocation entryPath, RegistryEntryInjector<?> injector) throws IOException {
        // Get all DataPack entries for the resource rather than just the 'top' one.
        final Collection<IResource> resources = manager.getAllResources(entryPath);
        if (resources.size() > 1) {
            Grout.LOG.debug("Merging datapack entries for: {}", entryKey);
        }

        // Functions for merging this type of json.
        final BinaryOperator<JsonElement> merger = injector.getMerger();
        final Function<JsonElement, Stream<JsonElement>> mapper = injector.getMapper();

        // Merge DataPack registry entries first.
        JsonElement result = JsonNull.INSTANCE;
        for (IResource resource : resources) {
            try (Closeable ignored = resource; Reader reader = newReader(resource)) {
                JsonElement json = parser.parse(reader);
                if (result.isJsonNull()) {
                    result = json;
                } else {
                    result = mapper.apply(json).reduce(result, merger);
                }
            }
        }

        if (!result.isJsonNull() && injector.hasChildren(entryKey)) {
            Grout.LOG.debug("Merging registered entries for: {}", entryKey);
            result = injector.injectChildren(entryKey, result);
        }

        return result;
    }

    private static ResourceLocation toJsonPath(RegistryKey<?> registryKey, ResourceLocation name) {
        return new ResourceLocation(
                name.getNamespace(),
                registryKey.getLocation().getPath() + "/" + name.getPath() + ".json"
        );
    }

    private static Reader newReader(IResource resource) {
        return new InputStreamReader(new BufferedInputStream(resource.getInputStream()), StandardCharsets.UTF_8);
    }
}
