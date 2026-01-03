package com.timbo.ancientcomputer;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.item.Items;

import com.timbo.ancientcomputer.blocks.ModBlocks;

public class AncientComputerBlockLootTableProvider extends FabricBlockLootTableProvider {
	protected AncientComputerBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, registryLookup);
	}

	@Override
	public void generate() {
		dropSelf(ModBlocks.ECHO_DUST_BLOCK);
		dropSelf(ModBlocks.COMPUTER_BLOCK);
	
		add(ModBlocks.ECHO_DUST_BLOCK, LootTable.lootTable().withPool(applyExplosionCondition(Items.OAK_LOG, LootPool.lootPool()
		.setRolls(new UniformGenerator(new ConstantValue(4f), new ConstantValue(6f)))
		.add(LootItem.lootTableItem(ModBlocks.ECHO_DUST_BLOCK.asItem()))))
);
	}
}