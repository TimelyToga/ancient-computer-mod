package com.timbo.tutorialmod;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initialization.
 * This runs only on the client (not dedicated servers).
 *
 * Use this for:
 *   - Custom renderers
 *   - Particle effects
 *   - GUI screens
 *   - Key bindings
 */
public class TutorialModClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    // Client-specific initialization goes here
    // For our simple block, we don't need anything special yet!
  }
}
