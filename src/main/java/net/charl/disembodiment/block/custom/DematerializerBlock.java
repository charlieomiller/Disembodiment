package net.charl.disembodiment.block.custom;

import net.charl.disembodiment.block.entity.DematerializerBlockEntity;
import net.charl.disembodiment.block.entity.ModBlockEntities;
import net.charl.disembodiment.config.ModConfigs;
import net.charl.disembodiment.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DematerializerBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public DematerializerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pHand == InteractionHand.MAIN_HAND
                && !isDematerializerFuel(itemstack)
                && isDematerializerFuel(pPlayer.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        }
        if (!isDematerializerFuel(itemstack)) { return InteractionResult.PASS; }

        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (!(be instanceof DematerializerBlockEntity dematerializerBE)) { return InteractionResult.CONSUME; }

            // Check if dematerializer can accept Inkor right now
            if (!dematerializerBE.canAcceptFuel(pPlayer.getUUID())) { return InteractionResult.CONSUME; }

            // Take Inkor if player not in creative
            if (!pPlayer.getAbilities().instabuild) { itemstack.shrink(1); }

            // Add time + reset buffer for player that successfully put in Inkor
            dematerializerBE.addInkorForPlayer(pPlayer.getUUID());

            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(pPlayer, pState)); // For sculk sensors..?
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DematerializerBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.DEMATERIALIZER_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));

    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof DematerializerBlockEntity demat && level instanceof ServerLevel sl) {
                    demat.onBroken(sl);
                }
            }
            super.onRemove(oldState, level, pos, newState, isMoving);
        } else {
            super.onRemove(oldState, level, pos, newState, isMoving);
        }
    }

    private static boolean isDematerializerFuel(ItemStack pStack) { return pStack.is(ModItems.INKOR.get()); }
}
