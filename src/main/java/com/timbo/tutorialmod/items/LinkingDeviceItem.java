package com.timbo.tutorialmod.items;

import com.timbo.tutorialmod.blocks.AncientComputerBlockEntity;
import com.timbo.tutorialmod.blocks.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LinkingDeviceItem extends Item {
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
            // We have a stored transmitter position - set up receiver
            CompoundTag tag = customData.copyTag();
            
            BlockPos transmitterPos = new BlockPos(
                tag.getIntOr("x", 0), 
                tag.getIntOr("y", 0), 
                tag.getIntOr("z", 0)
            );
            String storedDimension = tag.getStringOr("dimension", "");
            String currentDimension = world.dimension().registry().toString();

            // Don't allow linking to self
            if (transmitterPos.equals(pos) && storedDimension.equals(currentDimension)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Cannot link a computer to itself! Click a different computer.")
                            .withStyle(ChatFormatting.RED), 
                        true
                    );
                }
                return InteractionResult.FAIL;
            }

            // Check dimensions match
            if (!storedDimension.equals(currentDimension)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Cannot link computers across dimensions!")
                            .withStyle(ChatFormatting.RED), 
                        true
                    );
                }
                return InteractionResult.FAIL;
            }

            // Verify the transmitter still exists
            BlockEntity transmitterEntity = world.getBlockEntity(transmitterPos);
            if (!(transmitterEntity instanceof AncientComputerBlockEntity transmitter)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Source computer no longer exists! Select a new source.")
                            .withStyle(ChatFormatting.YELLOW), 
                        true
                    );
                }
                // Store this position as new transmitter
                storePosition(stack, pos, world);
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Source (input) selected: " + pos.toShortString() + " - Now click destination")
                            .withStyle(ChatFormatting.AQUA), 
                        true
                    );
                }
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

            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.literal("✓ Linked: " + transmitterPos.toShortString() + " (input) → " + pos.toShortString() + " (output)")
                        .withStyle(ChatFormatting.GREEN), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        } else {
            // No stored position - store this one as the transmitter (source)
            
            // If this computer is already linked, show its status
            if (computer.isLinked()) {
                BlockPos linkedPos = computer.getLinkedPos();
                String role = computer.isTransmitter() ? "input → " + linkedPos.toShortString() : linkedPos.toShortString() + " → output";
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("This computer is already linked as " + role + ". Selecting as new source...")
                            .withStyle(ChatFormatting.YELLOW), 
                        true
                    );
                }
            }
            
            storePosition(stack, pos, world);
            
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.literal("Source (input) selected: " + pos.toShortString() + " - Now click destination (output)")
                        .withStyle(ChatFormatting.AQUA), 
                    true
                );
            }
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
