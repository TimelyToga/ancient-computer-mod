package com.timbo.tutorialmod.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class AncientComputerBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos linkedPos = null;
    private int lastInputSignal = 0;

    // Data record for serialization
    public record Data(Optional<BlockPos> linkedPos, int lastInputSignal) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                BlockPos.CODEC.optionalFieldOf("linkedPos").forGetter(Data::linkedPos),
                Codec.INT.fieldOf("lastInputSignal").orElse(0).forGetter(Data::lastInputSignal)
            ).apply(instance, Data::new)
        );
    }

    public AncientComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCIENT_COMPUTER_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public BlockPos getLinkedPos() {
        return linkedPos;
    }

    public void setLinkedPos(@Nullable BlockPos pos) {
        this.linkedPos = pos;
        setChanged();
        if (level != null && !level.isClientSide()) {
            // Force full update of this block and neighbors
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_ALL);
            level.updateNeighborsAt(getBlockPos(), state.getBlock());
            
            // Also update the activated state immediately
            updateActivatedState();
        }
    }

    public void clearLink() {
        this.linkedPos = null;
        this.lastInputSignal = 0;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_ALL);
            level.updateNeighborsAt(getBlockPos(), state.getBlock());
            updateActivatedState();
        }
    }

    public boolean isLinked() {
        return linkedPos != null;
    }

    /**
     * Called when the input signal to this computer changes.
     * Propagates the signal to the linked computer.
     */
    public void onInputSignalChanged(int newSignal) {
        if (level == null || level.isClientSide()) return;
        
        if (newSignal != lastInputSignal) {
            lastInputSignal = newSignal;
            setChanged();
            
            // Update our own activated state
            updateActivatedState();
            
            // Update the linked computer's output
            if (linkedPos != null) {
                BlockEntity linkedEntity = level.getBlockEntity(linkedPos);
                if (linkedEntity instanceof AncientComputerBlockEntity linkedComputer) {
                    // Trigger a full update at the linked position
                    BlockState linkedState = level.getBlockState(linkedPos);
                    level.updateNeighborsAt(linkedPos, linkedState.getBlock());
                    linkedComputer.updateActivatedState();
                }
            }
        }
    }
    
    /**
     * Updates the ACTIVATED blockstate based on current signal states.
     */
    public void updateActivatedState() {
        if (level == null || level.isClientSide()) return;
        
        BlockState state = getBlockState();
        if (!state.hasProperty(ComputerPillarBlock.ACTIVATED)) return;
        
        boolean currentlyActivated = state.getValue(ComputerPillarBlock.ACTIVATED);
        boolean shouldBeActivated = lastInputSignal > 0 || getOutputSignal() > 0;
        
        if (currentlyActivated != shouldBeActivated) {
            level.setBlock(getBlockPos(), state.setValue(ComputerPillarBlock.ACTIVATED, shouldBeActivated), Block.UPDATE_ALL);
        }
    }

    /**
     * Gets the output signal strength based on the linked computer's input.
     */
    public int getOutputSignal() {
        if (level == null || linkedPos == null) return 0;
        
        BlockEntity linkedEntity = level.getBlockEntity(linkedPos);
        if (linkedEntity instanceof AncientComputerBlockEntity linkedComputer) {
            return linkedComputer.lastInputSignal;
        }
        return 0;
    }

    public int getLastInputSignal() {
        return lastInputSignal;
    }

    public Data getData() {
        return new Data(Optional.ofNullable(linkedPos), lastInputSignal);
    }

    public void setData(Data data) {
        this.linkedPos = data.linkedPos().orElse(null);
        this.lastInputSignal = data.lastInputSignal();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Tick method to check for redstone input changes.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, AncientComputerBlockEntity blockEntity) {
        if (level.isClientSide()) return;
        
        // Check the current redstone input
        int inputSignal = level.getBestNeighborSignal(pos);
        blockEntity.onInputSignalChanged(inputSignal);
    }
}
