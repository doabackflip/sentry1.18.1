package sentry.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;
import sentry.Sentry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SentryShooterTileEntity extends BlockEntity {
    private static final ThreadLocal<SentryShooterTileEntity> lastTile = new ThreadLocal<>();

    private LivingEntity target;
    private int attackTick = 0;
    private int updateTargetTick = 0;
    private final AABB bb;
    private UUID placerId;
    private Player placer;
    private final List<PlayerInfo> whiteList = new ArrayList<>();

    public Player getPlacer() {
        return placer;
    }

    public static class PlayerInfo {
        public final UUID uuid;
        public final String name;

        private PlayerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }

    public SentryShooterTileEntity(BlockPos pos, BlockState state) {
        super(Sentry.SENTRY_SHOOTER_TILE_ENTITY.get(), pos, state);
        lastTile.set(this);
        bb = new AABB(pos.getX() - 12, pos.getY() - 3, pos.getZ() - 12, pos.getX() + 12, pos.getY() + 10, pos.getZ() + 12);
    }


    public static void tick(Level level, BlockPos pos, BlockState state, SentryShooterTileEntity entity) {
        if (entity.shouldUpdateTarget()) {
            entity.updateTarget(level);
        }
        if (entity.shouldAttack()) {
            entity.attackTarget();
        }
    }

    public void addPlayerToWhiteList(Player e) {
        System.out.println("add " + e.getGameProfile().getName());
        this.whiteList.add(new PlayerInfo(e.getUUID(), e.getGameProfile().getName()));
    }

    public void removePlayerFromWhiteList(PlayerInfo e) {
        this.whiteList.remove(e);
    }

    public List<PlayerInfo> getWhiteList() {
        return whiteList;
    }

    private void attackTarget() {
        if (level == null) {
            return;
        }
        BlockEntity e = level.getBlockEntity(this.worldPosition.below(2));
        if (e == null) {
            return;
        }
        IItemHandler handler = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(EmptyHandler.INSTANCE);
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        for (int i = 0; i < handler.getSlots(); i++) {
            Vec3 direction = new Vec3(this.target.getX() - x, this.target.getY() - y, this.target.getZ() - z);
            direction = direction.normalize();
            ItemStack result = handler.extractItem(i, 1, true);
            if (result.getItem() instanceof ArrowItem) {
                handler.extractItem(i, 1, false);
                ArrowItem arrow = (ArrowItem) result.getItem();
                AbstractArrow entity;
                if (this.placer == null) {
                    entity = arrow.createArrow(this.level, result.getContainerItem(), target);
                } else {
                    entity = arrow.createArrow(this.level, result.getContainerItem(), this.placer);
                }
                entity.setNoGravity(true);
                entity.setPos(x + direction.x, y + direction.y, z + direction.z);
                entity.shoot(direction.x, direction.y, direction.z, 3, 1);
                this.level.addFreshEntity(entity);
                break;
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.placerId != null) {
            compound.put("placer", NbtUtils.createUUID(this.placerId));
        }
        ListTag list = new ListTag();
        for (PlayerInfo info : whiteList) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("uuid", info.uuid);
            tag.putString("name", info.name);
            list.add(tag);
        }
        compound.put("whitelist", list);
    }


    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        if (compound.hasUUID("placer")) {
            this.placerId = compound.getUUID("placer");
        }
        if (compound.contains("whitelist")) {
            ListTag whitelist = compound.getList("whitelist", 10);
            for (int i = 0; i < whitelist.size(); i++) {
                CompoundTag tag = whitelist.getCompound(i);
                this.whiteList.add(new PlayerInfo(tag.getUUID("uuid"), tag.getString("name")));
            }
        }
    }

    private boolean shouldAttack() {
        attackTick++;
        if (attackTick > 20) {
            attackTick = 0;
            return target != null;
        }
        return false;
    }

    private boolean isValidTarget(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof Player player) {
            if (whiteList.stream().noneMatch(c -> c.name.equalsIgnoreCase(player.getGameProfile().getName()))) {
                return true;
            }
        }
        return entity instanceof Monster
                || entity instanceof Slime || entity instanceof FlyingMob
                || entity instanceof EnderDragon
                || entity instanceof Shulker;
    }

    private boolean shouldUpdateTarget() {
        updateTargetTick++;
        if (updateTargetTick > 20) {
            updateTargetTick = 0;
            return true;
        }
        return false;
    }

    private void checkPlacer() {
        if (placer != null && !placer.isAlive()) {
            placer = null;
        }

        if (placer == null && this.placerId != null) {
            this.placer = level.getPlayerByUUID(this.placerId);
        }
    }

    private void updateTarget(Level level) {
        this.checkPlacer();
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        List<LivingEntity> entityList = (List) level.getEntities(placer, bb, this::isValidTarget);
        entityList.sort((a, b) -> (int) (b.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) - a.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ())));
        for (LivingEntity entity : entityList) {
            Vec3 vec3d = new Vec3(entity.getX() - x, entity.getY() - y, entity.getZ() - z);
            vec3d = vec3d.normalize();
            Vec3 src = vec3d.add(x, y, z);
            BlockHitResult result = level.clip(new ClipContext(entity.getEyePosition(), src, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (result.getType() == BlockHitResult.Type.MISS) {
                this.target = entity;
                return;
            }

        }
        this.target = null;
    }


    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isClientSide()) return;
        if (event.getPlacedBlock().getBlock() != Sentry.SENTRY_SHOOTER_BLOCK.get()) return;
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            SentryShooterTileEntity sentryShooterEntityBlock = lastTile.get();
            sentryShooterEntityBlock.placerId = player.getUUID();
            sentryShooterEntityBlock.placer = player;
        }
        lastTile.set(null);
    }

}
