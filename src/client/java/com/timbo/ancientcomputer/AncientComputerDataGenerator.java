package com.timbo.ancientcomputer;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AncientComputerDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(AncientComputerBlockLootTableProvider::new);
		pack.addProvider(AncientComputerModelProvider::new);
		pack.addProvider(AncientComputerRecipeProvider::new);
	}
}
