package com.github.wonton.grout.mixin;

import com.github.wonton.grout.Grout;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSettingsImport.class)
public class MixinWorldSettingsImport {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static <T> void onCreate(DynamicOps<T> ops, IResourceManager resources, DynamicRegistries.Impl registries, CallbackInfoReturnable<WorldSettingsImport<T>> cir) {
        // Inject our own IResourceAccess that handles merging of the datapack json + registered entries
        cir.setReturnValue(Grout.createSettingsImport(ops, resources, registries));
    }
}
