package com.github.wonton.grout.codec;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.inject.RegistryEntryInjector;
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

public class InjectorResourceAccess implements WorldSettingsImport.IResourceAccess {

    private final IResourceManager manager;
    private final JsonParser parser = new JsonParser();

    public InjectorResourceAccess(IResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public Collection<ResourceLocation> getRegistryObjects(RegistryKey<? extends Registry<?>> registryKey) {
        return manager.getAllResourceLocations(registryKey.getLocation().getPath(), file -> file.endsWith(".json"));
    }

    @Override
    public <E> DataResult<Pair<E, OptionalInt>> decode(DynamicOps<JsonElement> ops, RegistryKey<? extends Registry<E>> registryKey, RegistryKey<E> entryKey, Decoder<E> decoder) {
        final ResourceLocation entryName = entryKey.getLocation();
        final ResourceLocation entryPath = toJsonPath(registryKey, entryName);
        final RegistryEntryInjector<?, ?> injector = RegistryEntryInjector.getInjector(registryKey);

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

    @Override
    public String toString() {
        return "InjectorResourceAccess[" + manager + "]";
    }

    private JsonElement loadJson(RegistryKey<?> entryKey, ResourceLocation entryPath, @Nullable RegistryEntryInjector<?, ?> injector) throws IOException {
        if (injector == null) {
            return loadOne(entryPath);
        }

        return loadAll(entryKey, entryPath, injector);
    }

    private JsonElement loadOne(ResourceLocation entryPath) throws IOException {
        try (IResource resource = manager.getResource(entryPath)) {
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return parser.parse(reader);
            }
        }
    }

    private JsonElement loadAll(RegistryKey<?> entryKey, ResourceLocation entryPath, RegistryEntryInjector<?, ?> injector) throws IOException {
        final BinaryOperator<JsonElement> merger = injector.getEntryMerger();
        final Function<JsonElement, Stream<JsonElement>> mapper = injector.getEntryMapper();
        final Collection<IResource> resources = manager.getAllResources(entryPath);

        if (resources.size() > 1) {
            Grout.LOG.debug("Merging datapack registry entry: {}", entryKey);
        }

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

    public static <T> WorldSettingsImport<T> createSettingsImport(DynamicOps<T> ops, IResourceManager resourceManager, DynamicRegistries.Impl dynamicRegistries) {
        InjectorResourceAccess access = new InjectorResourceAccess(resourceManager);
        return WorldSettingsImport.create(ops, access, dynamicRegistries);
    }
}
