package com.timbo.tutorialmod;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

import com.timbo.tutorialmod.items.ModItems;

public class TutorialModModelProvider extends FabricModelProvider {
    public TutorialModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        // Block models will be added here
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        // Simple flat items
        itemModelGenerator.generateFlatItem(ModItems.SUSPICIOUS_SUBSTANCE, ModelTemplates.FLAT_ITEM);
        itemModelGenerator.generateFlatItem(ModItems.LINKING_DEVICE, ModelTemplates.FLAT_ITEM);
    }

    @Override
    public String getName() {
        return "TutorialModModelProvider";
    }
}

