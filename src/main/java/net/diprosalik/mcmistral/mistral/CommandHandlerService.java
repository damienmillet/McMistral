package net.diprosalik.mcmistral.mistral;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandHandlerService {

    public static String processResponseCommands(String responseText, CommandSourceStack source) {
        if (!responseText.contains("[COMMAND:")) {
            return responseText;
        }

        int start = responseText.indexOf("[COMMAND:");
        int end = responseText.indexOf("]", start);

        if (end == -1) {
            return responseText;
        }

        String command = responseText.substring(start + 9, end).trim();
        String cleanText = responseText.substring(0, start).trim();

        boolean isProtectedCommand = command.startsWith("teleport") || command.startsWith("tp")
                || command.startsWith("gamemode") || command.startsWith("give");

        if (isProtectedCommand && !source.hasPermission(2)) {
            source.sendFailure(Component.literal("Mistral tried to execute an admin command, but you lack Permission Level 2 (OP)!"));
            return cleanText;
        }

        source.getServer().execute(() -> {
            try {
                CommandSourceStack adminPlayerSource = source.withPermission(4).withSuppressedOutput();
                source.getServer().getCommands().performPrefixedCommand(adminPlayerSource, command);
                source.sendSuccess(() -> Component.literal("[Mistral executed: /" + command + "]").withStyle(ChatFormatting.GREEN), false);
            } catch (Exception e) {
                source.sendFailure(Component.literal("Failed to execute command: " + e.getMessage()));
            }
        });

        return cleanText;
    }
}
