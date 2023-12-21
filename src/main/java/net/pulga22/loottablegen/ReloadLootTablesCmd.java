package net.pulga22.loottablegen;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ReloadLootTablesCmd {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("chestloot").requires(source -> source.hasPermissionLevel(2))
                .then(
                        CommandManager.literal("reload").executes(ReloadLootTablesCmd::reload)
                )
        );
    }

    private static int reload(CommandContext<ServerCommandSource> ctx){
        ServerCommandSource source = ctx.getSource();
        if (!source.isExecutedByPlayer()) return -1;
        Config.getInstance().loadConfig();
        source.sendFeedback(Text.of("Loot tables reloaded."), true);
        return 1;
    }

}
