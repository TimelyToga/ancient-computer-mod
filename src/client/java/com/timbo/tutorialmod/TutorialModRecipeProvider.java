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
				// Suspicious Substance recipes
				shaped(RecipeCategory.MISC, suspiciousSubstanceItem)
					.pattern("  a")
					.pattern(" a ")
					.pattern("a  ")
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
				
				// Ancient Computer recipe
				// [Polished Diorite] [Polished Diorite] [Polished Diorite]
				// [Observer]         [Echo Shard]       [Observer]
				// [Polished Diorite] [Polished Diorite] [Polished Diorite]
				shaped(RecipeCategory.REDSTONE, ModBlocks.COMPUTER_BLOCK)
					.pattern("DDD")
					.pattern("OEO")
					.pattern("DDD")
					.define('D', Items.POLISHED_DIORITE)
					.define('E', Items.ECHO_SHARD)
					.define('O', Items.OBSERVER)
					.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
					.save(output);
				
				// Linking Device recipe
				// [   ] [Redstone] [   ]
				// [Redstone] [Echo Shard] [Redstone]
				// [   ] [Redstone] [   ]
				shaped(RecipeCategory.TOOLS, ModItems.LINKING_DEVICE)
					.pattern(" R ")
					.pattern("RER")
					.pattern(" R ")
					.define('R', Items.REDSTONE)
					.define('E', Items.ECHO_SHARD)
					.unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
					.save(output);
				
				// Echo Shard crafting recipe
				// [   ] [Suspicious Substance] [   ]
				// [Suspicious Substance] [Eye of Ender] [Suspicious Substance]
				// [   ] [Suspicious Substance] [   ]
				shaped(RecipeCategory.MISC, Items.ECHO_SHARD)
					.pattern(" S ")
					.pattern("SES")
					.pattern(" S ")
					.define('S', suspiciousSubstanceItem)
					.define('E', Items.ENDER_EYE)
					.unlockedBy("has_suspicious_substance", has(suspiciousSubstanceItem))
					.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, "echo_shard_from_suspicious_substance")));
			}
		};
	}
	@Override
	public String getName() {
		return "TutorialModRecipeProvider";
	}
}