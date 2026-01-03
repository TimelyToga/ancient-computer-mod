package com.timbo.tutorialmod;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

import com.timbo.tutorialmod.items.ModItems;
import com.timbo.tutorialmod.blocks.ModBlocks;

public class TutorialModRecipeProvider extends FabricRecipeProvider{
    
	public TutorialModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}
	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
		return new RecipeProvider(registryLookup, exporter) {
			HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

			HolderLookup.RegistryLookup<Block> blockLookup = registries.lookupOrThrow(Registries.BLOCK);
			ResourceKey<Item> suspiciousSubstanceItemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "suspicious_substance"));
			Item suspiciousSubstanceItem = itemLookup.getOrThrow(suspiciousSubstanceItemKey).value();
			ResourceKey<Block> suspiciousSubstanceBlockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "suspicious_substance_block"));
			 Block suspiciousSubstanceBlock = blockLookup.getOrThrow(suspiciousSubstanceBlockKey).value();


			@Override
			public void buildRecipes() {
				shaped(RecipeCategory.MISC, suspiciousSubstanceItem)
					.pattern("a a")
					.define('a', Items.AMETHYST_SHARD)
					.unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
					.save(output);
				shaped(RecipeCategory.MISC, suspiciousSubstanceBlock)
					.pattern("a")
					.pattern("a")
					.define('a', suspiciousSubstanceItem)
					.unlockedBy("has_suspicious_substance", has(suspiciousSubstanceItem))
					.save(output);	
				shapeless(RecipeCategory.MISC, suspiciousSubstanceItem, 2)
					.requires(suspiciousSubstanceBlock)
					.unlockedBy("has_suspicious_substance_block", has(suspiciousSubstanceBlock))
					.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "suspicious_substance_from_block")));
			}
		};
	}
	@Override
	public String getName() {
		return "TutorialModRecipeProvider";
	}
}