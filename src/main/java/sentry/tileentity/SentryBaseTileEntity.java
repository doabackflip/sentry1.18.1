package sentry.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import sentry.ItemStackSentryBaseHandler;
import sentry.Sentry;
import sentry.container.SentryBaseMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class SentryBaseTileEntity extends BlockEntity implements MenuProvider {
    private ItemStackSentryBaseHandler inventory;

    public SentryBaseTileEntity(BlockPos pos, BlockState state) {
        super(Sentry.SENTRY_BASE_TILE_ENTITY.get(), pos, state);
        this.inventory = new ItemStackSentryBaseHandler(5);
    }

    @SubscribeEvent
    public static void onAttachEntityBlock(AttachCapabilitiesEvent<BlockEntity> event) {
        if ((event.getObject()).getType() == Sentry.SENTRY_BASE_TILE_ENTITY.get()) {
            SentryBaseTileEntity tile = (SentryBaseTileEntity) event.getObject();
            event.addCapability(new ResourceLocation("sentry", "sentry/base"), new ICapabilitySerializable<CompoundTag>() {
                @Nonnull
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                        return (LazyOptional<T>) LazyOptional.of(tile::getInventory);
                    }
                    return LazyOptional.empty();
                }

                @Nonnull
                public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap) {
                    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                        return (LazyOptional<T>) LazyOptional.of(tile::getInventory);
                    }
                    return LazyOptional.empty();
                }

                public CompoundTag serializeNBT() {
                    return tile.inventory.serializeNBT();
                }

                public void deserializeNBT(final CompoundTag nbt) {
                    tile.inventory.deserializeNBT(nbt);
                }
            });
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.sentry_base");
    }


    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SentryBaseMenu(id, playerInventory, this.inventory,
                ContainerLevelAccess.create(this.level, this.getBlockPos()));
    }
}
