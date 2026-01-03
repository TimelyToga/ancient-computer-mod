package com.timbo.tutorialmod.blocks;

import com.timbo.tutorialmod.TutorialMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<AncientComputerBlockEntity> ANCIENT_COMPUTER_BLOCK_ENTITY = 
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "ancient_computer_block_entity"),
            FabricBlockEntityTypeBuilder.create(
                AncientComputerBlockEntity::new,
                ModBlocks.COMPUTER_BLOCK
            ).build()
        );

    public static void initialize() {
        TutorialMod.LOGGER.info("Registering Block Entities for " + TutorialMod.MOD_ID);
    }
}
