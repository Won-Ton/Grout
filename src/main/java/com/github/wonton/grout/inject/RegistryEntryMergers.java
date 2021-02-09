package com.github.wonton.grout.inject;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.codec.Codecs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public class RegistryEntryMergers {

    public static final BinaryOperator<JsonElement> SEPARATION_SETTING_MERGER = RegistryEntryMergers::injectSeparationSetting;
    public static final BinaryOperator<JsonElement> JIGSAW_POOL_ENTRY_MERGER = RegistryEntryMergers::injectJigsawPoolEntry;

    public static final Function<JsonElement, Stream<JsonElement>> SEPARATION_SETTING_MAPPER = RegistryEntryMergers::streamSeparationSettings;
    public static final Function<JsonElement, Stream<JsonElement>> JIGSAW_POOL_ENTRY_MAPPER = RegistryEntryMergers::streamJigsawPoolEntries;

    private static JsonElement injectSeparationSetting(JsonElement parent, JsonElement child) {
        if (!parent.isJsonObject() || !child.isJsonObject()) {
            return parent;
        }

        JsonElement name = Codecs.get(child, "name", "structure name");
        JsonElement setting = Codecs.get(child, "settings", "separation setting");
        JsonObject separationSettings = getSeparationSettings(parent.getAsJsonObject());

        if (!separationSettings.has(name.getAsString())) {
            separationSettings.add(name.getAsString(), setting);
            Grout.LOG.debug(" - Injected separation settings for structure: {}", name.getAsString());
        }

        return parent;
    }

    private static JsonElement injectJigsawPoolEntry(JsonElement parent, JsonElement child) {
        if (!parent.isJsonObject() || !child.isJsonObject()) {
            return parent;
        }

        final JsonArray rootElements = parent.getAsJsonObject().getAsJsonArray("elements");
        Objects.requireNonNull(rootElements, "elements");

        rootElements.add(child);

        return parent;
    }

    private static Stream<JsonElement> streamSeparationSettings(JsonElement entry) {
        if (!entry.isJsonObject()) {
            return Stream.empty();
        }

        JsonObject separationSettings = getSeparationSettings(entry.getAsJsonObject());

        return separationSettings.entrySet().stream().map(e -> {
            JsonObject pair = new JsonObject();
            pair.addProperty("name", e.getKey());
            pair.add("settings", e.getValue());
            return pair;
        });
    }

    private static Stream<JsonElement> streamJigsawPoolEntries(JsonElement json) {
        return Stream.empty();
    }

    private static JsonObject getSeparationSettings(JsonElement parent) {
        JsonObject structureSettings = Codecs.getObject(parent, "structures", "structure settings");
        return Codecs.getObject(structureSettings, "structures", "separation settings");
    }
}
