package com.timbo.tutorialmod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ComputerPillarBlock extends RotatedPillarBlock {
    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public ComputerPillarBlock(Properties settings) {
        super(settings);
        // Set the default state: axis=Y and activated=false
        registerDefaultState(defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(ACTIVATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // This adds AXIS from RotatedPillarBlock
        builder.add(ACTIVATED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) {
            // Skip if the player isn't allowed to modify the world.
            return InteractionResult.PASS;
        } else {
            // Get the current value of the "activated" property
            boolean activated = state.getValue(ACTIVATED);

            // Flip the value of activated and save the new blockstate.
            world.setBlockAndUpdate(pos, state.setValue(ACTIVATED, !activated));

            // Play a click sound to emphasise the interaction.
            world.playSound(player, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, activated ? 0.5F : 1.0F);

            return InteractionResult.SUCCESS;
        }
    }

    /**
     * Returns the light level based on the activated state.
     * Used when registering the block.
     */
    public static int getLuminance(BlockState currentBlockState) {
        boolean activated = currentBlockState.getValue(ACTIVATED);
        return activated ? 15 : 0;
    }
}

