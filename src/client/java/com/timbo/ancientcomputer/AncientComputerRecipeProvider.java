package com.timbo.ancientcomputer;

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

import com.timbo.ancientcomputer.items.ModItems;
import com.timbo.ancientcomputer.blocks.ModBlocks;

public class AncientComputerRecipeProvider extends FabricRecipeProvider{
    
	public AncientComputerRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}
	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
		return new RecipeProvider(registryLookup, exporter) {
			HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

			HolderLookup.RegistryLookup<Block> blockLookup = registries.lookupOrThrow(Registries.BLOCK);
			ResourceKey<Item> echoDustItemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, "echo_dust"));
			Item echoDustItem = itemLookup.getOrThrow(echoDustItemKey).value();
			ResourceKey<Block> echoDustBlockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, "echo_dust_block"));
			 Block echoDustBlock = blockLookup.getOrThrow(echoDustBlockKey).value();


			@Override
			public void buildRecipes() {
				// Echo Dust recipes
				shaped(RecipeCategory.MISC, echoDustItem)
					.pattern("  a")
					.pattern(" a ")
					.pattern("a  ")
					.define('a', Items.AMETHYST_SHARD)
					.unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
					.save(output);
				shaped(RecipeCategory.MISC, echoDustBlock)
					.pattern("a")
					.pattern("a")
					.define('a', echoDustItem)
					.unlockedBy("has_echo_dust", has(echoDustItem))
					.save(output);	
				shapeless(RecipeCategory.MISC, echoDustItem, 2)
					.requires(echoDustBlock)
					.unlockedBy("has_echo_dust_block", has(echoDustBlock))
					.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, "echo_dust_from_block")));
				
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
				// [   ] [Echo Dust] [   ]
				// [Echo Dust] [Eye of Ender] [Echo Dust]
				// [   ] [Echo Dust] [   ]
				shaped(RecipeCategory.MISC, Items.ECHO_SHARD)
					.pattern(" S ")
					.pattern("SES")
					.pattern(" S ")
					.define('S', echoDustItem)
					.define('E', Items.ENDER_EYE)
					.unlockedBy("has_echo_dust", has(echoDustItem))
					.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(AncientComputerMod.MOD_ID, "echo_shard_from_echo_dust")));
			}
		};
	}
	@Override
	public String getName() {
		return "AncientComputerRecipeProvider";
	}
}