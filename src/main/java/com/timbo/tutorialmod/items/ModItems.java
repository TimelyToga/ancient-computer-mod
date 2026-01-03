package com.timbo.tutorialmod.items;

import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import com.timbo.tutorialmod.TutorialMod;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class ModItems {
  public static final Item SUSPICIOUS_SUBSTANCE =
      register("suspicious_substance", Item::new, new Item.Properties());

  public static final Item LINKING_DEVICE =
      register("linking_device", LinkingDeviceItem::new, new Item.Properties().stacksTo(1));

  public static void initialize() {
    TutorialMod.LOGGER.info("Registering Mod Items for " + TutorialMod.MOD_ID);

    ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
      content.accept(SUSPICIOUS_SUBSTANCE);
      content.accept(LINKING_DEVICE);
    });
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