package sentry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sentry.AllowList;
import sentry.Sentry;
import sentry.tileentity.SentryBaseTileEntity;
import sentry.tileentity.SentryShooterTileEntity;

import java.util.Objects;

public class SentryBaseBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape MY_SHAPE;

    public SentryBaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState block, @NotNull Level worldIn, @NotNull BlockPos pos, Player player, @NotNull InteractionHand handIn, @NotNull BlockHitResult p_60508_) {
        if (player.getItemInHand(handIn).getItem() == Sentry.SENTRY_SUPP_BLOCK_ITEM.get()) {
            return InteractionResult.FAIL;
        }
        if (!worldIn.isClientSide) {
            BlockEntity e = worldIn.getBlockEntity(pos);
            if (e instanceof SentryBaseTileEntity) {
                player.openMenu((MenuProvider) e);

            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51778_) {
        p_51778_.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof SentryBaseTileEntity) {
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));

    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SentryBaseBlock.MY_SHAPE;
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
        if (worldIn.isClientSide) return;

        ServerLevel serverWorld = (ServerLevel) worldIn;
        MinecraftServer server = serverWorld.getServer();
        BlockPos blockPos = pos.above().above();
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
        VoxelShape voxelShape1 = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
        VoxelShape voxelShape2 = Block.box(4.0, 3.0, 4.0, 12.0, 16.0, 12.0);
        MY_SHAPE = Shapes.or(voxelShape1, voxelShape2);
    }


    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new SentryBaseTileEntity(p_153215_, p_153216_);
    }
}
