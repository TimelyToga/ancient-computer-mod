package com.timbo.tutorialmod.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

public class ComputerPillarBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerPillarBlock> CODEC = simpleCodec(ComputerPillarBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public ComputerPillarBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(ACTIVATED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, ACTIVATED);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return RotatedPillarBlock.rotatePillar(state, rotation);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientComputerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANCIENT_COMPUTER_BLOCK_ENTITY, AncientComputerBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AncientComputerBlockEntity computer) {
            // Show link status
            if (computer.isLinked()) {
                BlockPos linkedPos = computer.getLinkedPos();
                int signalLevel = computer.getCurrentSignalLevel();
                String role = computer.isTransmitter() ? "INPUT" : "OUTPUT";
                String arrow = computer.isTransmitter() ? " → " : " ← ";
                player.displayClientMessage(
                    Component.literal("[" + role + "] " + pos.toShortString() + arrow + linkedPos.toShortString() + " | Signal: " + signalLevel)
                        .withStyle(computer.isTransmitter() ? ChatFormatting.GOLD : ChatFormatting.AQUA),
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.literal("Not linked - Use Linking Device to connect")
                        .withStyle(ChatFormatting.YELLOW),
                    true
                );
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AncientComputerBlockEntity computer) {
            return computer.getOutputSignal();
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getSignal(state, world, pos, direction);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, world, pos, neighborBlock, orientation, movedByPiston);
        
        // Transmitters will pick up changes in their tick method
        // This is here for immediate response to neighbor changes
        if (!world.isClientSide()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AncientComputerBlockEntity computer && computer.isTransmitter()) {
                int signal = world.getBestNeighborSignal(pos);
                computer.transmitSignal(signal);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        // Initial state will be handled by tick
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean movedByPiston) {
        // Clear link from the linked computer before removal
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AncientComputerBlockEntity computer && computer.isLinked()) {
            BlockPos linkedPos = computer.getLinkedPos();
            BlockEntity linkedEntity = world.getBlockEntity(linkedPos);
            if (linkedEntity instanceof AncientComputerBlockEntity linkedComputer) {
                linkedComputer.clearLink();
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, movedByPiston);
    }

    /**
     * Returns the light level based on the activated state.
     */
    public static int getLuminance(BlockState currentBlockState) {
        boolean activated = currentBlockState.getValue(ACTIVATED);
        return activated ? 15 : 0;
    }
}
