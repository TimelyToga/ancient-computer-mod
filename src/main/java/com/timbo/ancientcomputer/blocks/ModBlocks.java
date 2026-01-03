package com.timbo.ancientcomputer.blocks;

import com.timbo.ancientcomputer.AncientComputerMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class ModBlocks {

    public static final Block ECHO_DUST_BLOCK = register("echo_dust_block", Block::new, BlockBehaviour.Properties.of().strength(1.0f), true);

    public static final Block COMPUTER_BLOCK = register(
            "computer_block",
            ComputerPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .lightLevel(ComputerPillarBlock::getLuminance),
            true
    );

   public static void initialize() {
    AncientComputerMod.LOGGER.info("Registering Mod Blocks for " + AncientComputerMod.MOD_ID);

    // Add to item group
    ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
        content.accept(ModBlocks.ECHO_DUST_BLOCK.asItem());
        content.accept(ModBlocks.COMPUTER_BLOCK.asItem());
    });
   }

   private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean hasItem) {
    ResourceKey<Block> blockKey = keyOfBlock(name);
    Block block = blockFactory.apply(settings.setId(blockKey));

    if (hasItem) {
        ResourceKey<Item> itemKey = keyOfItem(name);
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
    }

    Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

    return block;
   }

   private static ResourceKey<Block> keyOfBlock(String name) {
    return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, name));
   }

   private static ResourceKey<Item> keyOfItem(String name) {
    return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, name));
   }
}
