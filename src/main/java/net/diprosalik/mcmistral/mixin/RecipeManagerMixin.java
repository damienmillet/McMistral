package net.diprosalik.mcmistral.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.diprosalik.mcmistral.mistral.ModRecipeStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void onRecipesApplied(RecipeMap preparedRecipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        ModRecipeStorage.ALL_MOD_RECIPES.clear();
        Collection<RecipeHolder<?>> recipes = preparedRecipes.values();
        for (RecipeHolder<?> entry : recipes) {
            Identifier id = entry.id().identifier();
            if (id != null && !id.getNamespace().equals("minecraft")) {
                ModRecipeStorage.ALL_MOD_RECIPES.add(id.toString());
            }
        }
    }
}
