package net.pulga22.loottablegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.io.*;
import java.util.*;

public class Config {

    private boolean initialized = false;
    private File configFile;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private ConfigData data;

    private Config(){}

    private static class SingletonHolder {
        private static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(){
        if (initialized) return; //prevent initializing more than once
        initialized = true;
        configFile = FabricLoader.getInstance().getConfigDir().resolve("loottablegen.json").toFile();
        if (!configFile.exists()){
            data = new ConfigData();
            saveConfig();
            return;
        }
        loadConfig();
    }

    public void loadConfig(){
        try (FileReader reader = new FileReader(configFile)) {
            data = GSON.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            LootTableGen.LOGGER.error("Something went wrong loading config. Initializing with default config.");
            data = new ConfigData();
        }
    }

    public void saveConfig(){
        try(FileWriter fileWriter = new FileWriter(configFile)){
            GSON.toJson(data, fileWriter);
        } catch (IOException e) {
            LootTableGen.LOGGER.error("Something went wrong saving config.");
        }
    }

    /**
     * @param world World executing.
     * @param blockPos BlockPos where to get the biome.
     * @return A random loot table Identifier according to the corresponding biome of the blockPos.
     */
    public Optional<Identifier> getRandomLootTableFromPos(World world, BlockPos blockPos){
        //Get the Biome Identifier.
        Optional<RegistryKey<Biome>> optionalRegistryKey = world.getBiome(blockPos).getKey();
        if (optionalRegistryKey.isEmpty()) return Optional.empty();
        Identifier biomeIdentifier = optionalRegistryKey.get().getValue();

        //Get loot tables according to the biome.
        Optional<List<String>> lootTableOptionsOptional = getLootTablesFromBiomeId(biomeIdentifier);
        if (lootTableOptionsOptional.isEmpty()) return Optional.empty();
        List<String> lootTableOptions = lootTableOptionsOptional.get();

        //Check if there is an available option.
        if (lootTableOptions.isEmpty()) return Optional.empty();

        //Return a random loot table option.
        int randomItemIndex = world.random.nextInt(lootTableOptions.size());
        String randomItem = lootTableOptions.get(randomItemIndex);
        return Optional.ofNullable(Identifier.tryParse(randomItem));
    }

    /**
     * @param identifier Biome identifier.
     * @return A list of the loaded loot tables from the config according to the biome id. Returns the default set if not found.
     */
    public Optional<List<String>> getLootTablesFromBiomeId(Identifier identifier){
        String stringId = identifier.toString();
        if (!data.biomeKeys.containsKey(stringId)) { //If biomeKeys doesn't contain the id
            if (data.biomeKeys.containsKey("default")){ //but contains the "default" key word
                return Optional.of(data.biomeKeys.get("default")); //return the "default" options
            }
            return Optional.empty(); //else, return empty
        }
        return Optional.of(data.biomeKeys.get(stringId)); //return the options of the id
    }

    static final class ConfigData {
        public Map<String, List<String>> biomeKeys = new HashMap<>(){{
            put("default", new ArrayList<>());
        }};
    }

}
