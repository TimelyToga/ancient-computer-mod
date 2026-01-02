package com.timbo.tutorialmod.items;

import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.Registry;
import com.timbo.tutorialmod.TutorialMod;

public class ModItems {
  public static final Item SUSPICIOUS_SUBSTANCE =
      register("suspicious_substance", Item::new, new Item.Properties());

  public static void initialize() {
    LOGGER.info("Registering Mod Items for " + TutorialMod.MOD_ID);
  }

  public static <GenericItem extends Item> GenericItem register(
      String name, Function<Item.Properties, GenericItem> itemFactory, Item.Properties settings) {
    // Create the item key.
    ResourceKey<Item> itemKey = ResourceKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name));

    // Create the item instance.
    GenericItem item = itemFactory.apply(settings.setId(itemKey));

    // Register the item.
    Registry.register(BuiltInRegistries.ITEM, itemKey, item);

    return item;
  }
}