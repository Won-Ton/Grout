package com.github.wonton.grout;

import com.github.wonton.grout.inject.InjectorFunctions;
import com.github.wonton.grout.inject.RegistryEntryInjector;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.DimensionSettings;

public class Injectors {

    public static final RegistryEntryInjector<DimensionSettings> DIMENSION_SETTINGS = new RegistryEntryInjector<>(
            Registry.NOISE_SETTINGS_KEY,
            DimensionSettings.field_236097_a_,
            InjectorFunctions::addSeparationSetting,
            InjectorFunctions::getSeparationSettings
    );

    public static void reload() {
        Grout.LOG.info("Refreshing structure separation injections...");
        Injectors.DIMENSION_SETTINGS.registerAll(WorldGenRegistries.NOISE_SETTINGS, InjectorFunctions.MOD_STRUCTURE);
    }
}
