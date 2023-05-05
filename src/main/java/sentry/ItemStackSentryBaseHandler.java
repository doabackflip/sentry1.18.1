package sentry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Set;

public class ItemStackSentryBaseHandler extends ItemStackHandler {
    public ItemStackSentryBaseHandler(int size) {
        super(size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        Item item = stack.getItem().asItem();
        Set<ResourceLocation> tags = item.getTags();
        ResourceLocation arrows = new ResourceLocation("arrows");
        for(ResourceLocation tag:tags){
            if(tag.equals(arrows)){
                return true;
            }
        }
        return false;
    }
}
