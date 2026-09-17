package net.diprosalik.mcmistral.mistral;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class MistralJoinHandler {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(MistralJoinHandler::onPlayReady);
    }

    private static void onPlayReady(ServerGamePacketListenerImpl listener, PacketSender sender, net.minecraft.server.MinecraftServer server) {
        MistralConfig config = AutoConfig.getConfigHolder(MistralConfig.class).getConfig();
        if (!config.enableWelcomeGreeting) {
            return;
        }
        ServerPlayer player = listener.player;
        int gamesLeft = player.getStats().getValue(Stats.CUSTOM, Stats.LEAVE_GAME);
        String prompt = (gamesLeft == 0)
                ? "The player " + player.getName().getString() + " just joined this world for the VERY FIRST TIME. Introduce yourself briefly as their all-knowing AI companion, greet them warmly, and tell them you are ready to help them survive."
                : "The player " + player.getName().getString() + " just rejoined their existing world. Give them a very short, welcoming one-sentence welcome back greeting.";
        MistralClient.queryMistral(prompt, player.createCommandSourceStack()).thenAccept(response -> {
            player.sendSystemMessage(Component.literal("[Mistral]: ").withStyle(ChatFormatting.GOLD).append(Component.literal(response).withStyle(ChatFormatting.WHITE)), false);
        });
    }
}
