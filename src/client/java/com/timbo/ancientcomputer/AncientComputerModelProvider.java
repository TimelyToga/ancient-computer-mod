package com.timbo.ancientcomputer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

import com.timbo.ancientcomputer.items.ModItems;

public class AncientComputerModelProvider extends FabricModelProvider {
    public AncientComputerModelProvider(FabricDataOutput output) {
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
        return "AncientComputerModelProvider";
    }
}

