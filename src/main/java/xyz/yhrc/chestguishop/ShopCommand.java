package xyz.yhrc.chestguishop;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import xyz.yhrc.chestguishop.gui.ChestGuiScreenHandler;
import xyz.yhrc.chestguishop.storage.BarterShopEntry;
import xyz.yhrc.chestguishop.storage.ShopStorage;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.server.command.CommandManager.literal;
import static xyz.yhrc.chestguishop.ChestGuiShop.SHOP_STORAGE;
import static xyz.yhrc.chestguishop.ChestGuiShop.LOGGER;

public class ShopCommand {
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("shop.json");
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("shop").executes(ShopCommand::openGui)
                .then(literal("add")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ShopCommand::addItem))
                .then(literal("save")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ShopCommand::save))
                .then(literal("load")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ShopCommand::load)));
    }

    private static int save(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            var writer = new FileWriter(CONFIG_FILE.toFile());
            ShopStorage.Serializer.GSON.toJson(SHOP_STORAGE, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    private static int load(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            SHOP_STORAGE = ShopStorage.Serializer.GSON.fromJson(new FileReader(CONFIG_FILE.toFile()), ShopStorage.class);
        } catch (FileNotFoundException e) {
            LOGGER.info("Config file not found, creating a new one.");
            try {
                Files.createFile(CONFIG_FILE);
            } catch (IOException ex) {
                ex.printStackTrace();
                return 0;
            }
        }
        if (SHOP_STORAGE == null) {
            LOGGER.warn("Shop storage deserialization failed.");
            return 0;
        }
        return 1;
    }

    private static int addItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SHOP_STORAGE.add(new BarterShopEntry(context.getSource().getPlayer().getMainHandStack().copy()));
        return 1;
    }

    private static int openGui(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            return context.getSource().getPlayer().openHandledScreen(
                        new SimpleNamedScreenHandlerFactory(
                                (syncId, inv, player) -> SHOP_STORAGE.setupGui(new ChestGuiScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, inv, new SimpleInventory(54), 6)),
                                new TranslatableText("gui.shop.title")))
                .isPresent() ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
