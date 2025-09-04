package de.pasuki.glowing_glass;

import de.pasuki.glowing_glass.block.ModBlocks;
import de.pasuki.glowing_glass.config.Config;
import de.pasuki.glowing_glass.config.ConfigCache;
import de.pasuki.glowing_glass.item.ModCreativeModeTab;
import de.pasuki.glowing_glass.item.ModItems;
import net.neoforged.fml.ModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(GlowingGlass.MOD_ID)
public class GlowingGlass {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "glowing_glass";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public GlowingGlass(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (colorful_redstone_lamps) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTab.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);


        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Config registrieren
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        // WICHTIG: Auf Config-Load/Reload hören -> Cache füllen
        modEventBus.addListener(ConfigCache::onLoad);    // Loading
        modEventBus.addListener(ConfigCache::onReload);  // Reloading

    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
