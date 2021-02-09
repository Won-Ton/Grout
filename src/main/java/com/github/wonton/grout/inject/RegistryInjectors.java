package com.github.wonton.grout.inject;

import com.github.wonton.grout.codec.Codecs;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

public class RegistryInjectors {

    private static final RegistryEntryInjector<DimensionSettings, Pair<ResourceLocation, StructureSeparationSettings>> SEPARATION_SETTINGS = new RegistryEntryInjector<>(
            Registry.NOISE_SETTINGS_KEY,
            Codecs.DIMENSION_SETTINGS,
            Codecs.SEPARATION_SETTINGS,
            RegistryEntryMergers.SEPARATION_SETTING_MERGER,
            RegistryEntryMergers.SEPARATION_SETTING_MAPPER
    );

    private static final RegistryEntryInjector<JigsawPattern, Pair<JigsawPiece, Integer>> JIGSAW_POOLS = new RegistryEntryInjector<>(
            Registry.JIGSAW_POOL_KEY,
            Codecs.JIGSAW_PATTERN,
            Codecs.JIGSAW_POOL_ENTRY,
            RegistryEntryMergers.JIGSAW_POOL_ENTRY_MERGER,
            RegistryEntryMergers.JIGSAW_POOL_ENTRY_MAPPER
    );

    public static RegistryEntryInjector<JigsawPattern, Pair<JigsawPiece, Integer>> getJigsawPools() {
        return JIGSAW_POOLS;
    }

    public static RegistryEntryInjector<DimensionSettings, Pair<ResourceLocation, StructureSeparationSettings>> getSeparationSettings() {
        return SEPARATION_SETTINGS;
    }
}
