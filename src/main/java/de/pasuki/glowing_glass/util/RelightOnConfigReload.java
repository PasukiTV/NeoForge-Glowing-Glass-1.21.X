package de.pasuki.glowing_glass.util;

import de.pasuki.glowing_glass.GlowingGlass;
import de.pasuki.glowing_glass.block.ModBlocks;
import de.pasuki.glowing_glass.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Re-lightet bereits platzierte Glowing-Glass-Blöcke beim Config-(Re)Load.
 * Performant: nur geladene Sicht-Chunks rund um Spieler, pro Section früher Abbruch,
 * anschließender Scan relighted nur echte Treffer.
 */
@EventBusSubscriber(modid = GlowingGlass.MOD_ID) // bus weglassen → Default ist MOD
public final class RelightOnConfigReload {

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading e) {
        if (e.getConfig().getSpec() != Config.COMMON_SPEC) return;
        scheduleRelightAllLevels();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading e) {
        if (e.getConfig().getSpec() != Config.COMMON_SPEC) return;
        scheduleRelightAllLevels();
    }

    private static void scheduleRelightAllLevels() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        server.execute(() -> {
            for (ServerLevel level : server.getAllLevels()) {
                relightLoadedGlowingBlocks(level);
            }
        });
    }

    /** Kompatibel & flott: Sicht-Chunks → Sections mit frühem Abbruch → nur Treffer relighten. */
    private static void relightLoadedGlowingBlocks(ServerLevel level) {
        final var chunkSource = level.getChunkSource();
        final var lightEngine = chunkSource.getLightEngine();
        final int view = level.getServer().getPlayerList().getViewDistance();

        level.players().forEach(player -> {
            final int baseCX = player.chunkPosition().x;
            final int baseCZ = player.chunkPosition().z;

            for (int dx = -view; dx <= view; dx++) {
                for (int dz = -view; dz <= view; dz++) {
                    LevelChunk chunk = chunkSource.getChunk(baseCX + dx, baseCZ + dz, false);
                    if (chunk == null) continue;

                    final ChunkPos cpos = chunk.getPos();
                    final int minX = cpos.x << 4; // = getMinBlockX()
                    final int minZ = cpos.z << 4; // = getMinBlockZ()

                    LevelChunkSection[] sections = chunk.getSections();
                    for (int i = 0; i < sections.length; i++) {
                        LevelChunkSection section = sections[i];
                        if (section == null || section.hasOnlyAir()) continue;

                        // secMinY aus Section-Index ableiten (16 Blöcke pro Section)
                        final int secMinY = (chunk.getSectionYFromSectionIndex(i) << 4);

                        // --- 1) Früher Abbruch: prüfe, ob die Section ÜBERHAUPT einen unserer Blöcke enthält
                        boolean sectionHasAny = false;
                        outer:
                        for (int yOff = 0; yOff < 16; yOff++) {
                            int y = secMinY + yOff;
                            for (int xOff = 0; xOff < 16; xOff++) {
                                int x = minX + xOff;
                                for (int zOff = 0; zOff < 16; zOff++) {
                                    int z = minZ + zOff;
                                    BlockPos pos = new BlockPos(x, y, z);
                                    if (isGlowingGlass(level.getBlockState(pos).getBlock())) {
                                        sectionHasAny = true;
                                        break outer;
                                    }
                                }
                            }
                        }
                        if (!sectionHasAny) continue;

                        // --- 2) Section wirklich scannen und NUR Treffer relighten
                        for (int yOff = 0; yOff < 16; yOff++) {
                            int y = secMinY + yOff;
                            for (int xOff = 0; xOff < 16; xOff++) {
                                int x = minX + xOff;
                                for (int zOff = 0; zOff < 16; zOff++) {
                                    int z = minZ + zOff;
                                    BlockPos pos = new BlockPos(x, y, z);
                                    if (isGlowingGlass(level.getBlockState(pos).getBlock())) {
                                        lightEngine.checkBlock(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private static boolean isGlowingGlass(Block b) {
        if (b == ModBlocks.GLOWING_GLASS.get() || b == ModBlocks.GLOWING_GLASS_PANE.get())
            return true;
        for (var v : ModBlocks.COLORED_GLOWING_GLASS.values())
            if (b == v.get()) return true;
        for (var v : ModBlocks.COLORED_GLOWING_GLASS_PANES.values())
            if (b == v.get()) return true;
        return false;
    }

    private RelightOnConfigReload() {}
}
