package com.timbo.tutorialmod;

import com.timbo.tutorialmod.block.ModBlocks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialMod implements ModInitializer {
  public static final String MOD_ID = "tutorialmod";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    // Register all our custom blocks
    ModBlocks.register();

    LOGGER.info("Tutorial Mod initialized! Time to add some blocks!");
  }
}
