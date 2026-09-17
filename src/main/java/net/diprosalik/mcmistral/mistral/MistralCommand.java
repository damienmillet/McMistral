package net.diprosalik.mcmistral.mistral;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class MistralCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            dispatcher.register(Commands.literal("mistral")
                    .then(Commands.literal("apikey")
                            .then(Commands.argument("key", StringArgumentType.string())
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();
                                        String key = StringArgumentType.getString(context, "key");

                                        MistralConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(MistralConfig.class).getConfig();
                                        config.apiKey = key;
                                        me.shedaniel.autoconfig.AutoConfig.getConfigHolder(MistralConfig.class).save();

                                        source.sendSuccess(() -> Component.literal("API key has been set successfully.").withStyle(ChatFormatting.GREEN), false);
                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("ask")
                            .then(Commands.argument("prompt", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();
                                        String prompt = StringArgumentType.getString(context, "prompt");

                                        if (!MistralClient.hasApiKey()) {
                                            source.sendFailure(Component.literal("Please set an API key first using /mistral apikey <key>"));
                                            return 0;
                                        }

                                        if (source.getEntity() instanceof ServerPlayer player) {
                                            player.sendSystemMessage(Component.literal("[Mistral is thinking...]").withStyle(ChatFormatting.GRAY), true);
                                        }

                                        MistralClient.queryMistral(prompt, source).thenAccept(response -> {
                                            if (source.getEntity() instanceof ServerPlayer player) {
                                                player.sendSystemMessage(Component.literal(""), true);
                                                player.sendSystemMessage(Component.literal(""), false);
                                                player.sendSystemMessage(Component.literal("[Mistral]: ").withStyle(ChatFormatting.GOLD).append(Component.literal(response).withStyle(ChatFormatting.WHITE)), false);
                                            }
                                        });

                                        return 1;
                                    })
                            )
                    )
                    .then(Commands.literal("status")
                            .executes(context -> {
                                MistralConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(MistralConfig.class).getConfig();
                                if (!MistralClient.hasApiKey()) {
                                    context.getSource().sendSuccess(() -> Component.literal("Status: No API key set.").withStyle(ChatFormatting.RED), false);
                                } else if ("testapikey".equals(config.apiKey)) {
                                    context.getSource().sendSuccess(() -> Component.literal("Status: Simulation Mode Active.").withStyle(ChatFormatting.YELLOW), false);
                                } else {
                                    context.getSource().sendSuccess(() -> Component.literal("Status: Connected (Model: " + config.modelName + ")").withStyle(ChatFormatting.GREEN), false);
                                }
                                return 1;
                            })
                    )
                    .then(Commands.literal("clear")
                            .executes(context -> {
                                MistralConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(MistralConfig.class).getConfig();
                                config.apiKey = "";
                                me.shedaniel.autoconfig.AutoConfig.getConfigHolder(MistralConfig.class).save();

                                context.getSource().sendSuccess(() -> Component.literal("API key has been cleared and deleted from config.").withStyle(ChatFormatting.RED), false);
                                return 1;
                            })
                    )
                    .then(Commands.literal("help")
                            .executes(context -> {
                                context.getSource().sendSuccess(() -> Component.literal("=== Mistral Mod Commands ===").withStyle(ChatFormatting.GOLD), false);
                                context.getSource().sendSuccess(() -> Component.literal("/mistral apikey <key> - Set your token (or use 'testapikey')").withStyle(ChatFormatting.GRAY), false);
                                context.getSource().sendSuccess(() -> Component.literal("/mistral ask <prompt> - Ask the AI chatbot a question").withStyle(ChatFormatting.GRAY), false);
                                context.getSource().sendSuccess(() -> Component.literal("/mistral status        - Check connection status").withStyle(ChatFormatting.GRAY), false);
                                context.getSource().sendSuccess(() -> Component.literal("/mistral clear         - Remove the current API key").withStyle(ChatFormatting.GRAY), false);
                                return 1;
                            })
                    )
            );
        });
    }
}
