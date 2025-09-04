// de/pasuki/glowing_glass/config/ConfigCache.java
package de.pasuki.glowing_glass.config;

import de.pasuki.glowing_glass.GlowingGlass;
import net.neoforged.fml.event.config.ModConfigEvent;

public final class ConfigCache {
    public static int lightBlocks = 10;
    public static int lightPanes  = 10;

    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == Config.COMMON_SPEC) {
            lightBlocks = Config.LIGHT_LEVEL_BLOCKS.get();
            lightPanes  = Config.LIGHT_LEVEL_PANES.get();
            GlowingGlass.LOGGER.info("[GlowingGlass] Config LOAD -> blocks={}, panes={}", lightBlocks, lightPanes);
        }
    }

    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == Config.COMMON_SPEC) {
            lightBlocks = Config.LIGHT_LEVEL_BLOCKS.get();
            lightPanes  = Config.LIGHT_LEVEL_PANES.get();
            GlowingGlass.LOGGER.info("[GlowingGlass] Config RELOAD -> blocks={}, panes={}", lightBlocks, lightPanes);
        }
    }

    private ConfigCache() {}
}
