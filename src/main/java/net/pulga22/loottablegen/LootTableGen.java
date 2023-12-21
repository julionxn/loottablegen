package net.pulga22.loottablegen;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LootTableGen implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("loottablegen");

	@Override
	public void onInitialize() {
		Config.getInstance().init();
		CommandRegistrationCallback.EVENT.register(ReloadLootTablesCmd::register);
		LOGGER.info("Hello Fabric world!");
	}
}