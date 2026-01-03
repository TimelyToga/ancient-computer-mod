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

        // Check if we have a stored position
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        
        if (customData != null && customData.copyTag().contains("x")) {
            // We have a stored position - try to link
            CompoundTag tag = customData.copyTag();
            
            BlockPos storedPos = new BlockPos(
                tag.getIntOr("x", 0), 
                tag.getIntOr("y", 0), 
                tag.getIntOr("z", 0)
            );
            String storedDimension = tag.getStringOr("dimension", "");
            String currentDimension = world.dimension().registry().toString();

            // Don't allow linking to self
            if (storedPos.equals(pos) && storedDimension.equals(currentDimension)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Cannot link a computer to itself! Click a different computer.")
                            .withStyle(ChatFormatting.RED), 
                        true
                    );
                }
                return InteractionResult.FAIL;
            }

            // Check dimensions match (for simplicity, require same dimension)
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

            // Verify the stored block still exists and is an Ancient Computer
            BlockEntity storedEntity = world.getBlockEntity(storedPos);
            if (!(storedEntity instanceof AncientComputerBlockEntity otherComputer)) {
                if (context.getPlayer() != null) {
                    context.getPlayer().displayClientMessage(
                        Component.literal("Stored computer no longer exists! Storing this one instead.")
                            .withStyle(ChatFormatting.YELLOW), 
                        true
                    );
                }
                // Store this position instead
                storePosition(stack, pos, world);
                return InteractionResult.SUCCESS;
            }

            // Link the current computer to the stored one
            computer.setLinkedPos(storedPos);
            
            // Also link the stored computer to this one (bidirectional)
            otherComputer.setLinkedPos(pos);

            // Clear the stored position
            stack.remove(DataComponents.CUSTOM_DATA);

            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.literal("✓ Linked computers at " + storedPos.toShortString() + " ↔ " + pos.toShortString())
                        .withStyle(ChatFormatting.GREEN), 
                    true
                );
            }
            return InteractionResult.SUCCESS;
        } else {
            // No stored position - store this one
            storePosition(stack, pos, world);
            
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.literal("First computer selected: " + pos.toShortString() + " - Now click second computer")
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
