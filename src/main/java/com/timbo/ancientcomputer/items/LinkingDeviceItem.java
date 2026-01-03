package com.timbo.ancientcomputer.items;

import com.timbo.ancientcomputer.blocks.AncientComputerBlockEntity;
import com.timbo.ancientcomputer.blocks.ModBlocks;
import com.timbo.ancientcomputer.sounds.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LinkingDeviceItem extends Item {
    public static final int MAX_LINK_DISTANCE = 32;
    
    public LinkingDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        // Only work with Ancient Computer blocks
        if (!state.is(ModBlocks.COMPUTER_BLOCK)) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AncientComputerBlockEntity computer)) {
            return InteractionResult.PASS;
        }

        // Check if we have a stored position (transmitter)
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        
        if (customData != null && customData.copyTag().contains("x")) {
            CompoundTag tag = customData.copyTag();
            
            BlockPos transmitterPos = new BlockPos(
                tag.getIntOr("x", 0), 
                tag.getIntOr("y", 0), 
                tag.getIntOr("z", 0)
            );
            String storedDimension = tag.getStringOr("dimension", "");
            String currentDimension = world.dimension().registry().toString();

            // If not sneaking, just show status (don't complete link)
            if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
                // Show status
                world.playSound(null, pos, ModSounds.LINK_INQUIRY, SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (computer.isLinked()) {
                    BlockPos linkedPos = computer.getLinkedPos();
                    int signalLevel = computer.getCurrentSignalLevel();
                    String role = computer.isTransmitter() ? "INPUT" : "OUTPUT";
                    String arrow = computer.isTransmitter() ? " → " : " ← ";
                    if (context.getPlayer() != null) {
                        context.getPlayer().displayClientMessage(
                            Component.literal("[" + role + "] " + pos.toShortString() + arrow + linkedPos.toShortString() + " | Signal: " + signalLevel)
                                .withStyle(computer.isTransmitter() ? ChatFormatting.GOLD : ChatFormatting.AQUA),
                            true
                        );
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().displayClientMessage(
                            Component.literal("Pending link from " + transmitterPos.toShortString() + " - Shift+click to complete")
                                .withStyle(ChatFormatting.YELLOW),
                            true
                        );
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Shift+click - complete the link
            // Don't allow linking to self
            if (transmitterPos.equals(pos) && storedDimension.equals(currentDimension)) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Cannot link a computer to itself! Click a different computer.")
                        .withStyle(ChatFormatting.RED), 
                    true
                );
                return InteractionResult.FAIL;
            }

            // Check dimensions match
            if (!storedDimension.equals(currentDimension)) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Cannot link computers across dimensions!")
                        .withStyle(ChatFormatting.RED), 
                    true
                );
                return InteractionResult.FAIL;
            }

            // Check distance limit
            double distance = Math.sqrt(transmitterPos.distSqr(pos));
            if (distance > MAX_LINK_DISTANCE) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Too far! Max link distance is " + MAX_LINK_DISTANCE + " blocks (current: " + String.format("%.1f", distance) + ")")
                        .withStyle(ChatFormatting.RED), 
                    true
                );
                return InteractionResult.FAIL;
            }

            // Verify the transmitter still exists
            BlockEntity transmitterEntity = world.getBlockEntity(transmitterPos);
            if (!(transmitterEntity instanceof AncientComputerBlockEntity transmitter)) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Source computer no longer exists! Select a new source.")
                        .withStyle(ChatFormatting.YELLOW), 
                    true
                );
                // Store this position as new transmitter
                storePosition(stack, pos, world);
                context.getPlayer().displayClientMessage(
                    Component.literal("Source (input) selected: " + pos.toShortString() + " - Shift+click destination")
                        .withStyle(ChatFormatting.AQUA), 
                    true
                );
                return InteractionResult.SUCCESS;
            }

            // Clear any existing links
            if (transmitter.isLinked()) {
                BlockPos oldLinkedPos = transmitter.getLinkedPos();
                BlockEntity oldLinked = world.getBlockEntity(oldLinkedPos);
                if (oldLinked instanceof AncientComputerBlockEntity oldComputer) {
                    oldComputer.clearLink();
                }
            }
            if (computer.isLinked()) {
                BlockPos oldLinkedPos = computer.getLinkedPos();
                BlockEntity oldLinked = world.getBlockEntity(oldLinkedPos);
                if (oldLinked instanceof AncientComputerBlockEntity oldComputer) {
                    oldComputer.clearLink();
                }
            }

            // Set up the link: transmitter → receiver
            transmitter.setAsTransmitter(pos);      // First clicked = transmitter (input)
            computer.setAsReceiver(transmitterPos); // Second clicked = receiver (output)

            // Clear the stored position
            stack.remove(DataComponents.CUSTOM_DATA);

            // Play link_established sound at the receiver (second clicked computer)
            world.playSound(null, pos, ModSounds.LINK_ESTABLISHED, SoundSource.BLOCKS, 1.0f, 1.0f);

            context.getPlayer().displayClientMessage(
                Component.literal("✓ Linked: " + transmitterPos.toShortString() + " (input) → " + pos.toShortString() + " (output)")
                    .withStyle(ChatFormatting.GREEN), 
                true
            );
            return InteractionResult.SUCCESS;
        } else {
            // No stored position
            // Check if player is NOT sneaking - just show status
            if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
                // Normal right click = show status
                world.playSound(null, pos, ModSounds.LINK_INQUIRY, SoundSource.BLOCKS, 1.0f, 1.0f);
                
                if (computer.isLinked()) {
                    BlockPos linkedPos = computer.getLinkedPos();
                    int signalLevel = computer.getCurrentSignalLevel();
                    String role = computer.isTransmitter() ? "INPUT" : "OUTPUT";
                    String arrow = computer.isTransmitter() ? " → " : " ← ";
                    if (context.getPlayer() != null) {
                        context.getPlayer().displayClientMessage(
                            Component.literal("[" + role + "] " + pos.toShortString() + arrow + linkedPos.toShortString() + " | Signal: " + signalLevel)
                                .withStyle(computer.isTransmitter() ? ChatFormatting.GOLD : ChatFormatting.AQUA),
                            true
                        );
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().displayClientMessage(
                            Component.literal("Not linked - Shift+right click to start linking")
                                .withStyle(ChatFormatting.YELLOW),
                            true
                        );
                    }
                }
                return InteractionResult.SUCCESS;
            }
            
            // Shift + right click = start linking process
            // If this computer is already linked, show its status
            if (computer.isLinked()) {
                BlockPos linkedPos = computer.getLinkedPos();
                String role = computer.isTransmitter() ? "input → " + linkedPos.toShortString() : linkedPos.toShortString() + " → output";
                context.getPlayer().displayClientMessage(
                    Component.literal("This computer is already linked as " + role + ". Selecting as new source...")
                        .withStyle(ChatFormatting.YELLOW), 
                    true
                );
            }
            
            storePosition(stack, pos, world);
            
            // Play link_started sound at the first clicked computer
            world.playSound(null, pos, ModSounds.LINK_STARTED, SoundSource.BLOCKS, 1.0f, 1.0f);
            
            context.getPlayer().displayClientMessage(
                Component.literal("Source (input) selected: " + pos.toShortString() + " - Shift+click destination (output)")
                    .withStyle(ChatFormatting.AQUA), 
                true
            );
            return InteractionResult.SUCCESS;
        }
    }
    
    private void storePosition(ItemStack stack, BlockPos pos, Level world) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("dimension", world.dimension().registry().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
