package net.diprosalik.mcmistral.mistral;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MinecraftWorldContext {
    public static String buildContext(CommandSourceStack source) {
        StringBuilder context = new StringBuilder();
        var server = source.getServer();
        var world = source.getLevel();
        long clockTime = world.getOverworldClockTime();
        long dailyTime = clockTime % 24000;
        if (dailyTime < 0) dailyTime += 24000;
        long daysPlayed = clockTime / 24000;
        String worldPhase = evaluateWorldPhase(dailyTime);
        double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = Math.min(20.0, 1000.0 / mspt);
        context.append("=== MINECRAFT WORLD CONTEXT (DEBUG/F3 ACTIVE) ===\n");
        context.append("- Game Version: ").append(server.getServerVersion()).append("\n");
        context.append("- Server Performance: ").append(String.format("%.1f", tps)).append(" TPS / ").append(String.format("%.1f", mspt)).append(" MSPT\n");
        context.append("- Dimension: ").append(world.dimension().identifier().toString()).append("\n");
        context.append("- Days Passed in World: ").append(daysPlayed).append("\n");
        context.append("- Time Ticks: ").append(dailyTime).append(" / 24000\n");
        context.append("- World Phase: ").append(worldPhase).append("\n");
        context.append("- Weather: ").append(world.isThundering() ? "Thunderstorm" : (world.isRaining() ? "Raining/Snowing" : "Clear/Sunny")).append("\n");
        context.append("- Difficulty: ").append(world.getDifficulty().getSerializedName()).append("\n");
        context.append(" -Seed: ").append(((ServerLevel) world).getSeed()).append("\n");
        if (source.getEntity() instanceof ServerPlayer player) {
            Vec3 pos = player.position();
            BlockPos blockPos = player.blockPosition();
            ChunkPos chunkPos = ChunkPos.containing(blockPos);
            int skyLight = world.getBrightness(LightLayer.SKY, blockPos);
            int blockLight = world.getBrightness(LightLayer.BLOCK, blockPos);
            int totalLight = world.getRawBrightness(blockPos, 0);
            int seaLevel = world.getSeaLevel();
            boolean isInCave = skyLight == 0 && blockPos.getY() < seaLevel && !world.dimensionType().hasCeiling();
            context.append("\n=== F3 DEBUG NAVIGATION & LOCATION ===\n");
            context.append(String.format("- XYZ Coordinates: X: %.3f, Y: %.5f, Z: %.3f\n", pos.x, pos.y, pos.z));
            context.append(String.format("- Block Pos: [%d, %d, %d]\n", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            context.append(String.format("- Chunk Pos: [%d, %d] (In Chunk Local: X: %d, Y: %d, Z: %d)\n", chunkPos.x(), chunkPos.z(), blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15));
            context.append("- Facing Direction: ").append(player.getDirection().name().toUpperCase()).append(" (Yaw: ").append(String.format("%.1f", player.getYRot())).append(" / Pitch: ").append(String.format("%.1f", player.getXRot())).append(")\n");
            context.append(String.format("- F3 Light Level: %d (Sky: %d, Block: %d)\n", totalLight, skyLight, blockLight));
            context.append("- Is in Cave/Underground: ").append(isInCave).append("\n");
            context.append("- Sea Level Reference: ").append(seaLevel).append("\n");
            context.append("\n=== PLAYER STATUS ===\n");
            context.append("- Name: ").append(player.getName().getString()).append("\n");
            context.append("- Has permission Level 2: ").append(hasPermissionLevel(source, PermissionLevel.GAMEMASTERS)).append("\n");
            context.append("- Gamemode: ").append(player.gameMode().name()).append("\n");
            context.append("- Is on Ground: ").append(player.onGround()).append("\n");
            context.append("- Is Swimming: ").append(player.isSwimming()).append("\n");
            context.append("- Is Sneaking: ").append(player.isCrouching()).append("\n");
            context.append("- Health: ").append(String.format("%.1f", player.getHealth())).append("/").append(player.getMaxHealth()).append("\n");
            context.append("- Food Level: ").append(player.getFoodData().getFoodLevel()).append("/20 (Saturation: ").append(String.format("%.1f", player.getFoodData().getSaturationLevel())).append(")\n");
            context.append("- Experience: Level ").append(player.experienceLevel).append(" (Progress: ").append(String.format("%.1f", player.experienceProgress * 100)).append("%)\n");
            String biomeName = world.getBiome(blockPos).unwrapKey().map(k -> k.identifier().toString()).orElse("Unknown");
            context.append("- Current Biome: ").append(biomeName).append("\n");
            appendStatusEffects(context, player);
            context.append("- Main Hand: ").append(BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString()).append(" (Count: ").append(player.getMainHandItem().getCount()).append(")\n");
            context.append("- Off Hand: ").append(BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem()).toString()).append("\n");
            context.append("- Armor: [Helmet: ").append(BuiltInRegistries.ITEM.getKey(player.getItemBySlot(EquipmentSlot.HEAD).getItem()).toString()).append(", Chestplate: ").append(BuiltInRegistries.ITEM.getKey(player.getItemBySlot(EquipmentSlot.CHEST).getItem()).toString()).append(", Leggings: ").append(BuiltInRegistries.ITEM.getKey(player.getItemBySlot(EquipmentSlot.LEGS).getItem()).toString()).append(", Boots: ").append(BuiltInRegistries.ITEM.getKey(player.getItemBySlot(EquipmentSlot.FEET).getItem()).toString()).append("]\n");
            appendInventory(context, player);
            appendModRecipes(context);
            appendInstalledMods(context);
            appendTargetedBlock(context, player, world);
            context.append("\n=== IMMEDIATE ENVIRONMENT ===\n");
            context.append("- Block at Feet: ").append(BuiltInRegistries.BLOCK.getKey(world.getBlockState(blockPos).getBlock()).toString()).append("\n");
            context.append("- Block below Feet: ").append(BuiltInRegistries.BLOCK.getKey(world.getBlockState(blockPos.below()).getBlock()).toString()).append("\n");
            context.append("- Block above Head: ").append(BuiltInRegistries.BLOCK.getKey(world.getBlockState(blockPos.above(2)).getBlock()).toString()).append("\n");
            context.append("- Can see Sky: ").append(world.canSeeSky(blockPos)).append("\n");
        } else {
            context.append("\n- Executed by Console or Non-Player Entity.\n");
        }
        context.append("===============================\n");
        return context.toString();
    }

    private static String evaluateWorldPhase(long dailyTime) {
        if (dailyTime >= 0 && dailyTime < 9000) return "DAYTIME (Safe, no surface monster spawns)";
        if (dailyTime >= 9000 && dailyTime < 12000) return "SUNSET (Dusk, light dropping)";
        if (dailyTime >= 12000 && dailyTime < 13000) return "LATE SUNSET (Beds become usable)";
        if (dailyTime >= 13000 && dailyTime < 23000) return "NIGHTTIME (Hostile monsters spawn on surface, Beds are usable)";
        return "SUNRISE (Dawn, monsters start burning)";
    }

    private static void appendStatusEffects(StringBuilder context, ServerPlayer player) {
        List<MobEffectInstance> effects = player.getActiveEffects().stream().toList();
        if (!effects.isEmpty()) {
            String effectString = effects.stream()
                    .map(effect -> {
                        Identifier effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
                        String name = (effectId != null) ? effectId.toString() : "unknown";
                        return name + " (Amp: " + effect.getAmplifier() + ", Duration: " + (effect.getDuration() / 20) + "s)";
                    })
                    .collect(Collectors.joining(", "));
            context.append("- Active Status Effects: ").append(effectString).append("\n");
        } else {
            context.append("- Active Status Effects: None\n");
        }
    }

    private static void appendInventory(StringBuilder context, ServerPlayer player) {
        context.append("\n=== FULL PLAYER INVENTORY ===\n");
        var inventory = player.getInventory();
        boolean hasItems = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (i >= 36) continue;
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                hasItems = true;
                Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                context.append("- Slot ").append(i).append(": ").append(itemId.toString()).append(" (Count: ").append(stack.getCount()).append(")");
                if (stack.isDamageableItem()) {
                    context.append(" [Durability: ").append(stack.getMaxDamage() - stack.getDamageValue()).append("/").append(stack.getMaxDamage()).append("]");
                }
                context.append("\n");
            }
        }
        if (!hasItems) context.append("- Inventory is completely empty.\n");
    }

    private static void appendModRecipes(StringBuilder context) {
        context.append("\n=== CRAFTING KNOWLEDGE ===\n");
        context.append("- Available Mod Recipes: ");
        List<String> modRecipes = ModRecipeStorage.ALL_MOD_RECIPES;
        if (modRecipes.isEmpty()) {
            context.append("None detected.\n");
        } else {
            String recipeString = modRecipes.stream().limit(30).collect(Collectors.joining(", "));
            context.append(recipeString);
            if (modRecipes.size() > 30) {
                context.append("... (and ").append(modRecipes.size() - 30).append(" more)");
            }
            context.append("\n");
        }
    }

    private static void appendInstalledMods(StringBuilder context) {
        context.append("\n=== INSTALLED MODS & ITEMS ===\n");
        Map<String, List<String>> itemsByMod = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String namespace = id.getNamespace();
            if (!namespace.equals("minecraft") && !namespace.equals("brigadier")) {
                itemsByMod.computeIfAbsent(namespace, k -> new ArrayList<>()).add(id.getPath());
            }
        }
        if (itemsByMod.isEmpty()) {
            context.append("- No external custom item mods detected.\n");
        } else {
            for (Map.Entry<String, List<String>> entry : itemsByMod.entrySet()) {
                context.append("- Mod [").append(entry.getKey()).append("] provides items: ");
                List<String> items = entry.getValue();
                if (items.size() > 40) {
                    context.append(items.stream().limit(40).collect(Collectors.joining(", "))).append("... (and ").append(items.size() - 40).append(" more items)");
                } else {
                    context.append(String.join(", ", items));
                }
                context.append("\n");
            }
        }
    }

    private static void appendTargetedBlock(StringBuilder context, ServerPlayer player, Level world) {
        HitResult hit = player.pick(5.0, 0.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            BlockPos targetedPos = blockHit.getBlockPos();
            String targetedBlock = BuiltInRegistries.BLOCK.getKey(world.getBlockState(targetedPos).getBlock()).toString();
            context.append("- Looking at Block: ").append(targetedBlock).append("\n");
        }
    }

    private static boolean hasPermissionLevel(CommandSourceStack source, PermissionLevel required) {
        PermissionSet permissions = source.permissions();
        if (permissions instanceof LevelBasedPermissionSet levelBased) {
            return levelBased.level().isEqualOrHigherThan(required);
        }
        return permissions == PermissionSet.ALL_PERMISSIONS;
    }
}
