package com.github.wonton.grout.impl;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.injector.Injector;
import com.github.wonton.grout.injector.InjectorUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A DimensionSettings Injector that targets the StructureSeparationSettings map stored on:
 * > DimensionSettings >> DimensionStructureSettings >> Map<Structure<?>, StructureSeparationSettings>
 */
public final class StructureSeparationInjector implements Injector<DimensionSettings> {

    public static final StructureSeparationInjector ALL = new StructureSeparationInjector(name -> true);
    public static final StructureSeparationInjector VANILLA = new StructureSeparationInjector(name -> name.getNamespace().equals("minecraft"));
    public static final StructureSeparationInjector NON_VANILLA = new StructureSeparationInjector(name -> !name.getNamespace().equals("minecraft"));

    private final Predicate<ResourceLocation> filter;

    private StructureSeparationInjector(Predicate<ResourceLocation> filter) {
        this.filter = filter;
    }

    @Override
    public void inject(RegistryKey<DimensionSettings> key, JsonObject registryEntryJson) throws InjectionException {
        DimensionSettings settings = WorldGenRegistries.NOISE_SETTINGS.getValueForKey(key);
        if (settings == null) {
            return;
        }

        JsonObject separationSettingsJson = getSeparationSettingsJson(registryEntryJson);
        Map<Structure<?>, StructureSeparationSettings> separationSettings = settings.getStructures().func_236195_a_();

        for (Map.Entry<Structure<?>, StructureSeparationSettings> entry : separationSettings.entrySet()) {
            ResourceLocation registryName = entry.getKey().getRegistryName();
            if (registryName == null) {
                continue;
            }

            if (!filter.test(registryName)) {
                continue;
            }

            String name = registryName.toString();
            if (!separationSettingsJson.has(name)) {
                InjectorUtils.encode(entry.getValue(), StructureSeparationSettings.field_236664_a_).ifPresent(json -> {
                    Grout.LOG.debug(" - Injected structure separation settings for {}", name);
                    separationSettingsJson.add(name, json);
                });
            }
        }
    }

    @Override
    public void merge(JsonObject registryEntryDest, JsonObject registryEntrySrc) throws InjectionException {
        JsonObject separationSettingsJsonDest = getSeparationSettingsJson(registryEntryDest);
        JsonObject separationSettingsJsonSrc = getSeparationSettingsJson(registryEntrySrc);
        for (Map.Entry<String, JsonElement> entry : separationSettingsJsonSrc.entrySet()) {
            if (!separationSettingsJsonDest.has(entry.getKey())) {
                Grout.LOG.debug(" - Injected structure separation settings for {}", entry.getKey());
                separationSettingsJsonDest.add(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        return "StructureSeparationInjector{}";
    }

    protected static JsonObject getSeparationSettingsJson(JsonObject registryEntryJson) throws InjectionException {
        JsonObject structureSettingsJson = InjectorUtils.getObject(registryEntryJson, "structures"); // DimensionStructureSettings
        return InjectorUtils.getObject(structureSettingsJson, "structures"); // Map<Structure<?>, StructureSeparationSettings>
    }
}
