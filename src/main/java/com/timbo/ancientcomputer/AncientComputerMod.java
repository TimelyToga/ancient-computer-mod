package com.timbo.ancientcomputer;

import com.timbo.ancientcomputer.blocks.AncientComputerBlockEntity;
import com.timbo.ancientcomputer.blocks.ModBlockEntities;
import com.timbo.ancientcomputer.blocks.ModBlocks;
import com.timbo.ancientcomputer.items.ModItems;
import com.timbo.ancientcomputer.sounds.ModSounds;
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

public class AncientComputerMod implements ModInitializer {
  public static final String MOD_ID = "ancientcomputer";

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
    
    registerLinkingDeviceLeftClickCallback();
  }
  
  private void registerLinkingDeviceLeftClickCallback() {
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      // Check if player is holding the linking device
      if (!player.getItemInHand(hand).is(ModItems.LINKING_DEVICE)) {
        return InteractionResult.PASS;
      }
      
      // Check if the target block is a computer block
      if (!world.getBlockState(pos).is(ModBlocks.COMPUTER_BLOCK)) {
        return InteractionResult.PASS;
      }
      
      // Only process display on server side
      if (world.isClientSide()) {
        // Return SUCCESS on client to prevent mining animation
        return InteractionResult.SUCCESS;
      }
      
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (!(blockEntity instanceof AncientComputerBlockEntity computer)) {
        return InteractionResult.SUCCESS;
      }
      
      // Left click = show status (same as right click without shift)
      world.playSound(null, pos, ModSounds.LINK_INQUIRY, SoundSource.BLOCKS, 1.0f, 1.0f);
      
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
            Component.literal("Not linked - Shift+right click to start linking")
                .withStyle(ChatFormatting.YELLOW),
            true
        );
      }
      
      // Return SUCCESS to prevent the block from being mined
      return InteractionResult.SUCCESS;
    });
  }
}