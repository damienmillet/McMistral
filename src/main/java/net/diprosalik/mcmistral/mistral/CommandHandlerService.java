package net.diprosalik.mcmistral.mistral;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

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

        if (isProtectedCommand && !hasPermissionLevel(source, PermissionLevel.GAMEMASTERS)) {
            source.sendFailure(Component.literal("Mistral tried to execute an admin command, but you lack Permission Level 2 (OP)!"));
            return cleanText;
        }

        source.getServer().execute(() -> {
            try {
                CommandSourceStack adminPlayerSource = source.withPermission(LevelBasedPermissionSet.OWNER).withSuppressedOutput();
                source.getServer().getCommands().performPrefixedCommand(adminPlayerSource, command);
                source.sendSuccess(() -> Component.literal("[Mistral executed: /" + command + "]").withStyle(ChatFormatting.GREEN), false);
            } catch (Exception e) {
                source.sendFailure(Component.literal("Failed to execute command: " + e.getMessage()));
            }
        });

        return cleanText;
    }

    private static boolean hasPermissionLevel(CommandSourceStack source, PermissionLevel required) {
        PermissionSet permissions = source.permissions();
        if (permissions instanceof LevelBasedPermissionSet levelBased) {
            return levelBased.level().isEqualOrHigherThan(required);
        }
        return permissions == PermissionSet.ALL_PERMISSIONS;
    }
}
