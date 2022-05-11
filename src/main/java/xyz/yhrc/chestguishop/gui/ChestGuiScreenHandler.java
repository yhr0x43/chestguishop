package xyz.yhrc.chestguishop.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

// TODO this should be more generic than a Container. To make use of other ScreenHandlers
public class ChestGuiScreenHandler extends GenericContainerScreenHandler {
    private final List<Clickable> clickableList;

    public ChestGuiScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
        super(type, syncId, playerInventory, inventory, rows);
        clickableList = new ArrayList<>(rows * 9);
        for (int i = 0; i < rows * 9; i++)
            clickableList.add(Clickable.EMPTY);
    }

    public void setClickable(Clickable clickable, int col, int row) {
        if (col < 0 || col >= 9 || row < 0 || row >= this.getRows())
            throw new AssertionError(String.format("Column %d, row %d is out of range.", col, row));
        clickableList.set(col + row * 9, clickable);
    }

    public void setClickable(Clickable clickable, int slotIndex) {
        if (slotIndex >= this.getRows() * 9)
            throw new AssertionError(String.format("Slot index %d is out of range.", slotIndex));
        clickableList.set(slotIndex, clickable);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        getInventory().markDirty();
        if (0 <= slotIndex && slotIndex < this.getRows() * 9) {
            clickableList.get(slotIndex).onClick(button, actionType, player, this);
        } else {
            super.onSlotClick(slotIndex, button, actionType, player);
        }
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void sendContentUpdates() {
        for (ListIterator<Clickable> it = clickableList.listIterator(); it.hasNext(); ) {
            int index = it.nextIndex();
            ItemStack itemStack = it.next().icon();
            getInventory().setStack(index, itemStack);
        }
        super.sendContentUpdates();
    }
}