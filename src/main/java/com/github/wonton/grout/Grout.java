package com.github.wonton.grout;

import com.github.wonton.grout.impl.StructureSeparationInjector;
import com.github.wonton.grout.injector.RegistryEntryInjector;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod("grout")
public class Grout {

    public static final Logger LOG = LogManager.getLogger("Grout");

    public static GroutResourceAccess createResourceAccess(IResourceManager resourceManager) {
        return new GroutResourceAccess(resourceManager, getConfig(), getInjections());
    }

    // TODO: Make user-configurable
    public static GroutConfig getConfig() {
        return new GroutConfig(true, true);
    }

    public static Map<RegistryKey<? extends Registry<?>>, RegistryEntryInjector<?>> getInjections() {
        ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, RegistryEntryInjector<?>> builder = ImmutableMap.builder();

        builder.put(Registry.NOISE_SETTINGS_KEY, RegistryEntryInjector.builder(Registry.NOISE_SETTINGS_KEY)
                // TODO: Make filter choice user-configurable
                .add(StructureSeparationInjector.ALL)
                .build());

        return builder.build();
    }
}
