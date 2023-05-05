package sentry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import sentry.AllowList;
import sentry.Sentry;
import sentry.tileentity.SentryShooterTileEntity;

import java.util.Objects;
import java.util.Random;

public class SentrySuppBlock extends Block {
    public SentrySuppBlock(Block.Properties properties) {
        super(properties);
    }

    private static final VoxelShape MY_SHAPE;

//    @Override
//    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
//        return super.isValidSpawn(state, level, pos, type, entityType);
//    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos down = pos.below();
        if (level.getBlockState(down).getBlock() == Sentry.SENTRY_BASE_BLOCK.get()) {
            return true;
        }
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
        if (!state.canSurvive(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (!stateIn.canSurvive(worldIn, currentPos)) {
            worldIn.scheduleTick(currentPos, this, 1);
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SentrySuppBlock.MY_SHAPE;
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if (worldIn.isClientSide) return;

        ServerLevel serverWorld = (ServerLevel) worldIn;
        MinecraftServer server = serverWorld.getServer();
        BlockPos blockPos = pos.above();
        if (worldIn.getBlockEntity(blockPos) == null) return;

        SentryShooterTileEntity tile = (SentryShooterTileEntity) worldIn.getBlockEntity(blockPos);
        if (tile == null) return;

        Player placer = tile.getPlacer();
        if (placer == null) return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!Objects.equals(player.getUUID(), placer.getUUID()) || !(heldItem.getItem() instanceof WritableBookItem))
            return;

        AllowList.update(server, tile, placer, heldItem);
    }


    static {
        final VoxelShape voxelShape1 = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
        final VoxelShape voxelShape2 = Block.box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0);
        MY_SHAPE = Shapes.or(voxelShape1, voxelShape2);
    }
}
