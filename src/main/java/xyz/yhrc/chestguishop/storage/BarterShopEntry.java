package xyz.yhrc.chestguishop.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.OptionalInt;

public class BarterShopEntry extends ShopEntry {
    private final ItemStack stackCost;
    private final ItemStack stackReplenish;

    public BarterShopEntry(ItemStack stackForSale) {
        this(stackForSale, ItemStack.EMPTY, ItemStack.EMPTY);
    }
    public BarterShopEntry(ItemStack stackForSale, ItemStack stackCost, ItemStack stackReplenish) {
        super(stackForSale);
        this.stackCost = stackCost;
        this.stackReplenish = stackReplenish;
    }

    private static boolean tryTakeFromPlayer(PlayerEntity player, ItemStack stackToTake) {
        var stacksToTake = player.getInventory().main.stream()
                .filter(itemStack -> itemStack.isItemEqualIgnoreDamage(stackToTake)).toList();

        OptionalInt count = stacksToTake.stream().mapToInt(ItemStack::getCount).reduce(Integer::sum);

        if (count.isEmpty() || count.getAsInt() < stackToTake.getCount())
            return false;

        int itemsToTake = stackToTake.getCount();
        for (ItemStack itemStack : stacksToTake) {
            if (stackToTake.isItemEqualIgnoreDamage(itemStack)) {
                int currentCount = itemStack.getCount();
                if (currentCount < itemsToTake) {
                    itemStack.setCount(0);
                    itemsToTake -= currentCount;
                } else {
                    itemStack.setCount(currentCount - itemsToTake);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tryBuyFromPlayer(PlayerEntity player) {
        if (tryTakeFromPlayer(player, stackCost)) {
            player.getInventory().offerOrDrop(stackForSale.copy());
            return true;
        }
        return false;
    }

    @Override
    public boolean trySellToPlayer(PlayerEntity player) {
        if (tryTakeFromPlayer(player, stackForSale)) {
            player.getInventory().offerOrDrop(stackReplenish.copy());
            return true;
        }
        return false;
    }

    @Override
    public List<Text> lore() {
        return List.of(
                new TranslatableText("gui.shop.cost", stackCost.getCount(), new TranslatableText(stackCost.getTranslationKey()))
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)),
                new TranslatableText("gui.shop.replenish", stackCost.getCount(), new TranslatableText(stackCost.getTranslationKey()))
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)));
    }

    public ItemStack getStackCost() {
        return stackCost;
    }

    public ItemStack getStackReplenish() {
        return stackReplenish;
    }
}
