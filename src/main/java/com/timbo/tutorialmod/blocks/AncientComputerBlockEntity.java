package com.timbo.tutorialmod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class AncientComputerBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos linkedPos = null;
    private boolean isTransmitter = false;  // true = sends signal, false = receives signal
    private int currentSignalLevel = 0;     // The signal level this computer is handling

    public AncientComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCIENT_COMPUTER_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public BlockPos getLinkedPos() {
        return linkedPos;
    }
    
    public boolean isTransmitter() {
        return isTransmitter;
    }
    
    public boolean isReceiver() {
        return linkedPos != null && !isTransmitter;
    }

    /**
     * Sets up this computer as a transmitter linked to the given receiver position.
     */
    public void setAsTransmitter(BlockPos receiverPos) {
        this.linkedPos = receiverPos;
        this.isTransmitter = true;
        this.currentSignalLevel = 0;
        markDirtyAndUpdate();
    }
    
    /**
     * Sets up this computer as a receiver linked to the given transmitter position.
     */
    public void setAsReceiver(BlockPos transmitterPos) {
        this.linkedPos = transmitterPos;
        this.isTransmitter = false;
        this.currentSignalLevel = 0;
        markDirtyAndUpdate();
    }

    public void clearLink() {
        this.linkedPos = null;
        this.isTransmitter = false;
        this.currentSignalLevel = 0;
        markDirtyAndUpdate();
        updateActivatedState();
    }

    public boolean isLinked() {
        return linkedPos != null;
    }
    
    private void markDirtyAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_ALL);
        }
    }

    /**
     * Called by the transmitter when its input signal changes.
     * Updates the receiver to output this signal.
     */
    public void transmitSignal(int signalLevel) {
        if (level == null || level.isClientSide()) return;
        if (!isTransmitter || linkedPos == null) return;
        
        if (signalLevel != currentSignalLevel) {
            currentSignalLevel = signalLevel;
            setChanged();
            
            // Update our activated state (transmitter lights up when sending signal)
            updateActivatedState();
            
            // Tell the receiver to update
            BlockEntity linkedEntity = level.getBlockEntity(linkedPos);
            if (linkedEntity instanceof AncientComputerBlockEntity receiver) {
                receiver.receiveSignal(signalLevel);
            }
        }
    }
    
    /**
     * Called on the receiver when the transmitter sends a new signal.
     */
    public void receiveSignal(int signalLevel) {
        if (level == null || level.isClientSide()) return;
        if (isTransmitter) return;  // Only receivers handle this
        
        if (signalLevel != currentSignalLevel) {
            currentSignalLevel = signalLevel;
            setChanged();
            
            // Update our activated state (receiver lights up when outputting signal)
            updateActivatedState();
            
            // Notify neighbors that our redstone output changed
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }
    
    /**
     * Updates the ACTIVATED blockstate based on current signal.
     */
    public void updateActivatedState() {
        if (level == null || level.isClientSide()) return;
        
        BlockState state = getBlockState();
        if (!state.hasProperty(ComputerPillarBlock.ACTIVATED)) return;
        
        boolean currentlyActivated = state.getValue(ComputerPillarBlock.ACTIVATED);
        boolean shouldBeActivated = currentSignalLevel > 0;
        
        if (currentlyActivated != shouldBeActivated) {
            level.setBlock(getBlockPos(), state.setValue(ComputerPillarBlock.ACTIVATED, shouldBeActivated), Block.UPDATE_ALL);
        }
    }

    /**
     * Gets the output signal strength (only receivers output signal).
     */
    public int getOutputSignal() {
        if (!isLinked() || isTransmitter) return 0;
        return currentSignalLevel;
    }
    
    /**
     * Gets the current signal level for display purposes.
     */
    public int getCurrentSignalLevel() {
        return currentSignalLevel;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Tick method - transmitters check for redstone input changes.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, AncientComputerBlockEntity blockEntity) {
        if (level.isClientSide()) return;
        
        // Only transmitters check for input signal
        if (blockEntity.isTransmitter && blockEntity.linkedPos != null) {
            int inputSignal = level.getBestNeighborSignal(pos);
            blockEntity.transmitSignal(inputSignal);
        }
    }
}
