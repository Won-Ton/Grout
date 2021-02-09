package com.github.wonton.grout;

import com.github.wonton.grout.inject.RegistryInjectors;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("grout")
public class Grout {

    public static final Logger LOG = LogManager.getLogger("Grout");

    public Grout() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(Grout::reload);
    }

    public static void reload() {
        LOG.info("Registering structure separation injections...");
        RegistryInjectors.getSeparationSettings().registerAll(WorldGenRegistries.NOISE_SETTINGS);
    }
}
