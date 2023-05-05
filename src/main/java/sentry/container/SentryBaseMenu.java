package sentry.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import sentry.ItemStackSentryBaseHandler;
import sentry.Sentry;

public class SentryBaseMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    public SentryBaseMenu(int id, Inventory inventory) {
        this(id, inventory, new ItemStackSentryBaseHandler(5), ContainerLevelAccess.NULL);
    }

    public SentryBaseMenu(int id, Inventory inventory, IItemHandlerModifiable itemHandlerModifiable, ContainerLevelAccess access) {
        super(Sentry.SENTRY_BASE_CONTAINER.get(), id);
        this.access = access;

        for (int j = 0; j < 3; ++j) {
            this.addSlot(new SlotItemHandler(itemHandlerModifiable, j, 44 + 18 + j * 18, 20));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 109));
        }
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return stillValid(this.access, p_38874_, Sentry.SENTRY_BASE_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
