package xyz.yhrc.chestguishop.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public record Clickable(ItemStack icon, ClickCallback callback) {
    public static final Clickable EMPTY = new Clickable(ItemStack.EMPTY, ((button, actionType, player, gui) -> {}));

    public void onClick(int button, SlotActionType actionType, PlayerEntity player, ChestGuiScreenHandler gui) {
        callback.onClick(button, actionType, player, gui);
    }

    @FunctionalInterface
    public interface ClickCallback {
        void onClick(int button, SlotActionType actionType, PlayerEntity player, ChestGuiScreenHandler gui);
    }
}