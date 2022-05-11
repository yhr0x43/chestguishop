package xyz.yhrc.chestguishop.storage;

import com.google.gson.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import xyz.yhrc.chestguishop.ChestGuiShop;

import java.lang.reflect.Type;
import java.util.List;

public abstract class ShopEntry {
    protected ItemStack stackForSale;

    public ShopEntry(ItemStack itemStack) {
        stackForSale = itemStack;
    }

    abstract boolean tryBuyFromPlayer(PlayerEntity player);

    abstract boolean trySellToPlayer(PlayerEntity player);

    abstract List<Text> lore();

    public static class Serializer implements JsonSerializer<ShopEntry>, JsonDeserializer<ShopEntry> {
        public static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(ShopEntry.class, new Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(ItemStack.class, new ItemSerializer());
            return gsonBuilder.create();
        });

        @Override
        public JsonElement serialize(ShopEntry shopEntry, @NotNull Type type, JsonSerializationContext context) {
            var json = new JsonObject();
            // FIXME this is not modular enough
            switch (type.getTypeName()) {
                case "BarterShopEntry":
                    BarterShopEntry barterShopEntry = (BarterShopEntry) shopEntry;
                    json.addProperty("type", "barter");
                    json.add("stackForSale", GSON.toJsonTree(barterShopEntry.stackForSale));
                    json.add("stackCost", GSON.toJsonTree(barterShopEntry.getStackCost()));
                    json.add("stackReplenish", GSON.toJsonTree(barterShopEntry.getStackReplenish()));
                    return json;
                default:
                    ChestGuiShop.LOGGER.error(type.getTypeName());
            }
            return null;
        }

        @Override
        public ShopEntry deserialize(@NotNull JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            var jsonObject = json.getAsJsonObject();
            switch (jsonObject.get("type").getAsString()) {
                case "barter":
                    return new BarterShopEntry(
                            GSON.fromJson(jsonObject.getAsJsonObject("stackForSale"), ItemStack.class),
                            GSON.fromJson(jsonObject.getAsJsonObject("stackCost"), ItemStack.class),
                            GSON.fromJson(jsonObject.getAsJsonObject("stackReplenish"), ItemStack.class));
            }
            return null;
        }

        /**
         * This Inner class is meant to be used only for the serialization
         */
        private static class ItemSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
            // TODO: nbt
            @Override
            public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
                var json = new JsonObject();
                json.addProperty("id", itemStack.getItem().toString());
                json.addProperty("count", itemStack.getCount());
                return json;
            }

            @Override
            public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                return new ItemStack(
                        Registry.ITEM.get(new Identifier("minecraft", json.getAsJsonObject().get("id").getAsString())),
                        json.getAsJsonObject().get("count").getAsInt());
            }
        }
    }
}
