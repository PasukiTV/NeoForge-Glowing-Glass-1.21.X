package de.pasuki.glowing_glass.util;

import de.pasuki.glowing_glass.GlowingGlass;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class RelightOnServerTick {

    // Wird im Mod-Konstruktor via NeoForge.EVENT_BUS.addListener(...) registriert.
    public static void onServerTick(ServerTickEvent.Post e) {
        if (!RelightOnConfigReload.pendingRelight) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        RelightOnConfigReload.pendingRelight = false;
        server.execute(() -> {
            GlowingGlass.LOGGER.debug("[GlowingGlass] Pending relight executed on server tick.");
            for (var level : server.getAllLevels()) {
                RelightOnConfigReload.relightLoadedGlowingBlocks(level);
            }
        });
    }

    private RelightOnServerTick() {}
}
