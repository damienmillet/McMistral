package net.diprosalik.mcmistral.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public class McMistralClient implements ClientModInitializer {
    private static KeyMapping askKeyMapping;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.parse("key.mcmistral.category"));
        askKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mcmistral.ask",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (askKeyMapping.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new ChatScreen("/mistral ask ", false));
                }
            }
        });
    }
}
