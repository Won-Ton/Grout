package com.github.wonton.grout.inject;

import com.github.wonton.grout.Grout;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class RegistryEntryMergers {

    public static final BinaryOperator<JsonElement> SEPARATION_SETTING_MERGER = RegistryEntryMergers::injectSeparationSetting;
    public static final BinaryOperator<JsonElement> JIGSAW_POOL_ENTRY_MERGER = RegistryEntryMergers::injectJigsawPoolEntry;

    public static final Function<JsonElement, Stream<JsonElement>> SEPARATION_SETTING_MAPPER = RegistryEntryMergers::streamSeparationSettings;
    public static final Function<JsonElement, Stream<JsonElement>> JIGSAW_POOL_ENTRY_MAPPER = RegistryEntryMergers::streamJigsawPoolEntries;

    private static JsonElement injectSeparationSetting(JsonElement root, JsonElement entry) {
        if (!root.isJsonObject() || !entry.isJsonObject()) {
            return root;
        }

        final JsonObject structureSeparations = root.getAsJsonObject()
                .getAsJsonObject("structures")  // DimensionStructuresSettings
                .getAsJsonObject("structures"); // Map<Structure<?>, StructureSeparationSettings>

        final String name = entry.getAsJsonObject().get("name").getAsString();
        final JsonElement setting = entry.getAsJsonObject().get("settings");

        if (!structureSeparations.has(name)) {
            structureSeparations.add(name, setting);
            Grout.LOG.debug(" - Injected separation settings for structure: {}", name);
        }

        return root;
    }

    private static JsonElement injectJigsawPoolEntry(JsonElement root, JsonElement entry) {
        if (!root.isJsonObject() || !entry.isJsonObject()) {
            return root;
        }

        final JsonArray rootElements = root.getAsJsonObject().getAsJsonArray("elements");
        rootElements.add(entry);

        return root;
    }

    private static Stream<JsonElement> streamSeparationSettings(JsonElement entry) {
        if (!entry.isJsonObject()) {
            return Stream.empty();
        }

        final JsonObject structures = entry.getAsJsonObject()
                .getAsJsonObject("structures")  // DimensionStructuresSettings
                .getAsJsonObject("structures");

        return structures.entrySet().stream().map(e -> {
            JsonObject pair = new JsonObject();
            pair.addProperty("name", e.getKey());
            pair.add("settings", e.getValue());
            return pair;
        });
    }

    private static Stream<JsonElement> streamJigsawPoolEntries(JsonElement json) {
        return Stream.empty();
    }
}
