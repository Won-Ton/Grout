package com.github.wonton.grout.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

import java.util.function.Consumer;

public class Codecs {

    public static final Codec<DimensionSettings> DIMENSION_SETTINGS = DimensionSettings.field_236097_a_;
    public static final Codec<Pair<ResourceLocation, StructureSeparationSettings>> SEPARATION_SETTINGS = Codec.mapPair(
            ResourceLocation.CODEC.fieldOf("name"),
            StructureSeparationSettings.field_236664_a_.fieldOf("settings")
    ).codec();

    public static final Codec<JigsawPattern> JIGSAW_PATTERN = JigsawPattern.field_236852_a_;
    public static final Codec<Pair<JigsawPiece, Integer>> JIGSAW_POOL_ENTRY = Codec.mapPair(
            JigsawPiece.field_236847_e_.fieldOf("element"),
            Codec.INT.fieldOf("weight")
    ).codec();

    public static <E> void encode(E entry, Codec<E> codec, Consumer<JsonElement> consumer) {
        codec.encodeStart(JsonOps.INSTANCE, entry).result().ifPresent(consumer);
    }

    public static <E> void decode(JsonElement json, Codec<E> codec, Consumer<E> consumer) {
        codec.decode(JsonOps.INSTANCE, json).map(Pair::getFirst).result().ifPresent(consumer);
    }
}
