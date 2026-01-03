package com.timbo.tutorialmod;

import com.timbo.tutorialmod.blocks.AncientComputerBlockEntity;
import com.timbo.tutorialmod.blocks.ModBlockEntities;
import com.timbo.tutorialmod.blocks.ModBlocks;
import com.timbo.tutorialmod.items.ModItems;
import com.timbo.tutorialmod.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialMod implements ModInitializer {
  public static final String MOD_ID = "tutorialmod";

  // This logger is used to write text to the console and the log file.
  // It is considered best practice to use your mod id as the logger's name.
  // That way, it's clear which mod wrote info, warnings, and errors.
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.

    LOGGER.info("Hello Fabric world!");
    ModItems.initialize();
    ModBlocks.initialize();
    ModBlockEntities.initialize();
    ModSounds.initialize();
    
    registerLinkBreakingCallback();
  }
  
  private void registerLinkBreakingCallback() {
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      // Only process on server side
      if (world.isClientSide()) {
        return InteractionResult.PASS;
      }
      
      // Check if player is holding the linking device
      if (!player.getItemInHand(hand).is(ModItems.LINKING_DEVICE)) {
        return InteractionResult.PASS;
      }
      
      // Check if the target block is a computer block
      if (!world.getBlockState(pos).is(ModBlocks.COMPUTER_BLOCK)) {
        return InteractionResult.PASS;
      }
      
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (!(blockEntity instanceof AncientComputerBlockEntity computer)) {
        return InteractionResult.PASS;
      }
      
      // Only break link if the computer is linked
      if (!computer.isLinked()) {
        player.displayClientMessage(
            Component.literal("This computer is not linked.")
                .withStyle(ChatFormatting.GRAY),
            true
        );
        // Still return SUCCESS to prevent mining with the linking device
        return InteractionResult.SUCCESS;
      }
      
      // Get the linked computer and break the link on both ends
      BlockPos linkedPos = computer.getLinkedPos();
      BlockEntity linkedEntity = world.getBlockEntity(linkedPos);
      
      if (linkedEntity instanceof AncientComputerBlockEntity linkedComputer) {
        linkedComputer.clearLink();
        // Play link_broken sound at the linked computer too
        world.playSound(null, linkedPos, ModSounds.LINK_BROKEN, SoundSource.BLOCKS, 1.0f, 1.0f);
      }
      computer.clearLink();
      
      // Play link_broken sound at this computer
      world.playSound(null, pos, ModSounds.LINK_BROKEN, SoundSource.BLOCKS, 1.0f, 1.0f);
      
      player.displayClientMessage(
          Component.literal("✗ Link broken!")
              .withStyle(ChatFormatting.RED),
          true
      );
      
      // Return SUCCESS to prevent the block from being mined
      return InteractionResult.SUCCESS;
    });
  }
}