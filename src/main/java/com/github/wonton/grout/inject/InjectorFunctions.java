package com.github.wonton.grout.inject;

import com.github.wonton.grout.Grout;
import com.github.wonton.grout.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Predicate;
import java.util.stream.Stream;

public final class InjectorFunctions {

    public static final Predicate<JsonElement> ALL_AVAILABLE = element -> true;
    public static final Predicate<JsonElement> MOD_STRUCTURE = element -> element.isJsonObject()
            && element.getAsJsonObject().has("name")
            && !element.getAsJsonObject().get("name").getAsString().startsWith("minecraft:");

    public static JsonElement addSeparationSetting(JsonElement parent, JsonElement child) {
        JsonElement name = Utils.get(child, "name", "structure name");
        JsonElement setting = Utils.get(child, "settings", "separation setting");
        JsonObject separationSettings = getSettings(parent);
        if (!separationSettings.has(name.getAsString())) {
            separationSettings.add(name.getAsString(), setting);
            Grout.LOG.debug(" - Injected separation settings for structure: {}", name.getAsString());
        }
        return parent;
    }

    public static Stream<JsonElement> getSeparationSettings(JsonElement entry) {
        return getSettings(entry.getAsJsonObject()).entrySet().stream().map(e -> {
            JsonObject pair = new JsonObject();
            pair.addProperty("name", e.getKey());
            pair.add("settings", e.getValue());
            return pair;
        });
    }

    private static JsonObject getSettings(JsonElement parent) {
        JsonObject structureSettings = Utils.getObject(parent, "structures", "structure settings");
        return Utils.getObject(structureSettings, "structures", "separation settings");
    }
}
