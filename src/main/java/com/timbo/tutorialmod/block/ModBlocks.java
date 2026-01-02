package com.timbo.tutorialmod.block;

import com.timbo.tutorialmod.TutorialMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * This class handles registration of all custom blocks in our mod.
 *
 * In Fabric, you register things by adding them to Minecraft's registries.
 * Each block needs:
 *   1. The Block itself (defines behavior)
 *   2. A BlockItem (so players can hold/place it)
 *   3. Assets: texture, model, blockstate JSON files
 */
public class ModBlocks {
  // ═══════════════════════════════════════════════════════════════════
  // AMETHYST LAMP - A decorative block that emits light
  // ═══════════════════════════════════════════════════════════════════
  public static final Block AMETHYST_LAMP = registerBlock("amethyst_lamp",
      id
      -> new Block(BlockBehaviour.Properties.of()
              .setId(id) // Required in 1.21.x - sets the block's registry key
              .strength(0.3f) // How hard it is to break (glass is 0.3)
              .sound(SoundType.AMETHYST) // What sound it makes
              .lightLevel(state -> 15) // Emits max light (like glowstone)
          ));

  // ═══════════════════════════════════════════════════════════════════
  // REGISTRATION HELPERS
  // ═══════════════════════════════════════════════════════════════════

  /**
   * Functional interface for creating blocks with their ResourceKey.
   */
  @FunctionalInterface
  private interface BlockFactory {
    Block create(ResourceKey<Block> key);
  }

  /**
   * Registers a block and automatically creates a BlockItem for it.
   * In 1.21.x, blocks need their ID set in the Properties before construction.
   */
  private static Block registerBlock(String name, BlockFactory factory) {
    // Create the resource key for this block
    Identifier id = Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name);
    ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);

    // Create the block with its key
    Block block = factory.create(blockKey);

    // Register the block
    Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

    // Create and register the BlockItem
    ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
    BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
    Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

    return block;
  }

  /**
   * Called from TutorialMod.onInitialize() to trigger static initialization
   * and add blocks to creative tabs.
   */
  public static void register() {
    TutorialMod.LOGGER.info("Registering blocks for " + TutorialMod.MOD_ID);

    // Add our blocks to the Building Blocks creative tab
    ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
      entries.accept(AMETHYST_LAMP);
    });
  }
}
