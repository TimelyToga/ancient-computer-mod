package com.timbo.tutorialmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class TutorialModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(TutorialModBlockLootTableProvider::new);
		// pack.addProvider(TutorialModBlockStateProvider::new);
		// pack.addProvider(TutorialModItemModelProvider::new);
		pack.addProvider(TutorialModRecipeProvider::new);
	}
}
