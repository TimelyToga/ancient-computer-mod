package com.timbo.tutorialmod.sounds;

import com.timbo.tutorialmod.TutorialMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent LINK_STARTED = register("link_started");
    public static final SoundEvent LINK_ESTABLISHED = register("link_established");
    public static final SoundEvent LINK_BROKEN = register("link_broken");
    public static final SoundEvent LINK_UPDATED = register("link_updated");
    public static final SoundEvent LINK_INQUIRY = register("link_inquiry");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(TutorialMod.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        TutorialMod.LOGGER.info("Registering Mod Sounds for " + TutorialMod.MOD_ID);
    }
}

