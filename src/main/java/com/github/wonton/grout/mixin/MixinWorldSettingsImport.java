package com.github.wonton.grout.mixin;

import com.github.wonton.grout.codec.InjectorResourceAccess;
import com.github.wonton.grout.inject.RegistryEntryInjector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.registry.WorldSettingsImport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSettingsImport.class)
public class MixinWorldSettingsImport {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static <T> void _create(DynamicOps<T> ops, IResourceManager resources, DynamicRegistries.Impl registries, CallbackInfoReturnable<WorldSettingsImport<T>> cir) {
        // Inject our own IResourceAccess that handles merging of the datapack json
        cir.setReturnValue(InjectorResourceAccess.createSettingsImport(ops, resources, registries));
    }

    @Inject(
            method = "decode(Lnet/minecraft/util/registry/SimpleRegistry;Lnet/minecraft/util/RegistryKey;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/DataResult;",
            at = @At("RETURN")
    )
    private <E> void _decode(SimpleRegistry<E> registry, RegistryKey<? extends Registry<E>> key, Codec<E> codec, CallbackInfoReturnable<DataResult<SimpleRegistry<E>>> cir) {
        // Apply the registry injector if one exists for this registry
        cir.getReturnValue().result().ifPresent(RegistryEntryInjector::inject);
    }
}
