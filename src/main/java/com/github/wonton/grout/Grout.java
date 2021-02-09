package com.github.wonton.grout;

import com.github.wonton.grout.inject.MergingResourceAccess;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("grout")
public class Grout {

    public static final Logger LOG = LogManager.getLogger("Grout");

    public static <T> WorldSettingsImport<T> createSettingsImport(DynamicOps<T> ops, IResourceManager resourceManager, DynamicRegistries.Impl dynamicRegistries) {
        // Make sure injectors have up-to-date info
        Injectors.reload();

        // Our IResourceAccess handles merging the json
        MergingResourceAccess access = new MergingResourceAccess(resourceManager);

        // A useful method that Mojang didn't make private, nice!
        return WorldSettingsImport.create(ops, access, dynamicRegistries);
    }
}
