package xyz.yhrc.chestguishop.storage;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.yhrc.chestguishop.gui.Clickable;
import xyz.yhrc.chestguishop.gui.ChestGuiScreenHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ShopStorage {
    private final ArrayList<ShopEntry> entries = new ArrayList<>();

    public ChestGuiScreenHandler setupGui(ChestGuiScreenHandler gui) {
        return setupGui(gui, 0);
    }

    @Contract("_, _ -> param1")
    private static @NotNull ItemStack addLore(@NotNull ItemStack itemStack, List<Text> lore) {
        var display = itemStack.getOrCreateSubNbt("display");
        var loreNbt = display.getList("Lore", NbtElement.STRING_TYPE);
        lore.forEach(text -> loreNbt.add(NbtString.of(Text.Serializer.toJson(text))));
        display.put("Lore", loreNbt);
        itemStack.setSubNbt("display", display);
        return itemStack;
    }

    public ChestGuiScreenHandler setupGui(@NotNull ChestGuiScreenHandler gui, int page) {
        assert(gui.getInventory().size() == 54);
        populateEntries(gui, page);
        populatePageControls(gui, page);
        return gui;
    }

    private void populateEntries(ChestGuiScreenHandler gui, int page) {
        List<ShopEntry> onDisplay = entries.subList(page * 45, Integer.min((page + 1) * 45, entries.size()));
        IntStream.range(0, onDisplay.size()).forEach(index -> {
            ShopEntry entry = onDisplay.get(index);
            gui.setClickable(new Clickable(
                    addLore(entry.stackForSale.copy(), entry.lore()),
                    (button, actionType, player, thisGui) -> {
                        boolean ret = false;
                        if (button == 0) ret = entry.tryBuyFromPlayer(player);  // Left click
                        if (button == 1) ret = entry.trySellToPlayer(player);   // Right click
                        player.sendMessage(new TranslatableText(ret ? "purchase.shop.success" : "purchase.shop.fail"), false);
                    }), index);});
        IntStream.range(onDisplay.size(), 54).forEach(index -> gui.setClickable(Clickable.EMPTY, index));
    }

    private void populatePageControls(ChestGuiScreenHandler gui, int page) {
        if (page > 0)
            gui.setClickable(new Clickable(
                    new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setCustomName(new TranslatableText("gui.shop.prev")),
                    (button, actionType, player, thisGui) -> setupGui(gui, page - 1)),
                    0, 5);

        if (page < (entries.size() / 45))
            gui.setClickable(new Clickable(
                    new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setCustomName(new TranslatableText("gui.shop.next")),
                    (button, actionType, player, thisGui) -> setupGui(gui, page + 1)),
                    8, 5);
    }

    public void add(ShopEntry entry) {
        entries.add(entry);
    }

    public static class Serializer implements JsonSerializer<ShopStorage>, JsonDeserializer<ShopStorage> {
        public static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(ShopStorage.class, new ShopStorage.Serializer());
            return gsonBuilder.create();
        });

        @Override
        public JsonElement serialize(@NotNull ShopStorage shopStorage, Type typeOfSrc, JsonSerializationContext context) {
            return shopStorage.entries.stream().map(ShopEntry.Serializer.GSON::toJsonTree).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        }

        @Override
        public ShopStorage deserialize(@NotNull JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            var storage = new ShopStorage();
            json.getAsJsonArray().forEach(jsonElement -> storage.add(ShopEntry.Serializer.GSON.fromJson(jsonElement, ShopEntry.class)));
            return storage;
        }
    }
}
