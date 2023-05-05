package sentry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import sentry.tileentity.SentryShooterTileEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AllowList {
    public static void update(MinecraftServer server, SentryShooterTileEntity tile, Player placer, ItemStack heldItem) {
        List<SentryShooterTileEntity.PlayerInfo> whiteList = tile.getWhiteList();
        CompoundTag tag = heldItem.getTag();
        if (tag == null) return;

        byte resolved = tag.getByte("resolved");
        if (resolved != 0) return;

        ListTag pages = tag.getList("pages", 8);
        List<String> newList = pages.stream().map(p -> Arrays.stream(p.getAsString().split("\n")).filter(v -> v.length() > 0))
                .reduce(Stream::concat)
                .orElse(Stream.empty()).toList();

        newList.stream()
                .filter((name) -> whiteList.stream().noneMatch(v -> Objects.equals(v.name, name)))
                .map((name) -> server.getPlayerList().getPlayerByName(name))
                .filter(Objects::nonNull)
                .forEach(tile::addPlayerToWhiteList);
        whiteList.stream()
                .filter((info) -> newList.stream().noneMatch(v -> Objects.equals(v, info.name)))
                .forEach(tile::removePlayerFromWhiteList);

        for (SentryShooterTileEntity.PlayerInfo playerInfo : whiteList) {
            placer.sendMessage(new TextComponent(playerInfo.name + " is added to allow list!"), placer.getUUID());
        }
    }
}
