package xyz.yhrc.chestguishop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.yhrc.chestguishop.storage.ShopStorage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;


public class ChestGuiShop implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("chestguishop");

    // TODO setup a storage manager instead of this static variable
    public static ShopStorage SHOP_STORAGE = new ShopStorage();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ShopCommand.register(dispatcher));

        var configFile = FabricLoader.getInstance().getConfigDir().resolve("shop.json");
        try {
            SHOP_STORAGE = ShopStorage.Serializer.GSON.fromJson(new FileReader(configFile.toFile()), ShopStorage.class);
        } catch (FileNotFoundException e) {
            LOGGER.info("Config file not found, creating a new one.");
            try {
                Files.createFile(configFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            if (SHOP_STORAGE == null) {
                LOGGER.warn("Shop storage deserialization failed.");
                SHOP_STORAGE = new ShopStorage();
            }
        }
    }
}
