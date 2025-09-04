package de.pasuki.glowing_glass.interaction;

import de.pasuki.glowing_glass.GlowingGlass;
import de.pasuki.glowing_glass.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3f;

import java.util.Map;

@EventBusSubscriber(modid = GlowingGlass.MOD_ID)
public final class GlowToggleHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        // nur Main-Hand (verhindert doppeltes Feuern)
        if (e.getHand() != InteractionHand.MAIN_HAND) return;

        Level level = e.getLevel();
        if (level.isClientSide()) return;

        Player player = e.getEntity();
        ItemStack held = e.getItemStack();
        BlockPos pos = e.getPos();
        BlockState state = level.getBlockState(pos);

        boolean sneaking = player.isShiftKeyDown();

        // 1) Vanilla -> Glowing (RMB mit Glowstone Dust)
        if (held.is(Items.GLOWSTONE_DUST)) {
            if (tryVanillaToGlowing(level, pos, state)) {
                if (!player.isCreative()) held.shrink(1);
                feedback(level, pos);
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
                return;
            }
        }

        // 2) Glowing -> Vanilla (Sneak + RMB mit leerer Hand)
        if (sneaking && held.isEmpty()) {
            if (tryGlowingToVanilla(level, pos, state)) {
                Block.popResource(level, pos, new ItemStack(Items.GLOWSTONE_DUST));
                feedback(level, pos);
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
        }
    }

    /* ================= Helpers ================= */

    private static boolean tryVanillaToGlowing(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // Bereits glowing? -> Nichts tun
        if (isGlowingBlock(block)) return false;

        // Nur Vanilla-Blöcke (Namespace "minecraft") akzeptieren
        if (!isVanillaBlock(block)) return false;

        // Klarer Glas-Block
        if (block == Blocks.GLASS) {
            level.setBlock(pos, ModBlocks.GLOWING_GLASS.get().defaultBlockState(), 3);
            relight(level, pos);
            GlowingGlass.LOGGER.info("[GlowingGlass] setBlock at {} -> lightBlocks={}, lightPanes={}",
                    pos, de.pasuki.glowing_glass.config.ConfigCache.lightBlocks,
                    de.pasuki.glowing_glass.config.ConfigCache.lightPanes);

            return true;
        }
        // Klare Pane
        if (block == Blocks.GLASS_PANE) {
            BlockState newState = ModBlocks.GLOWING_GLASS_PANE.get().defaultBlockState();
            level.setBlock(pos, copyPaneConnections(state, newState), 3);
            relight(level, pos);
            // Klarer Glas-Block
            if (block == Blocks.GLASS) {
                level.setBlock(pos, ModBlocks.GLOWING_GLASS.get().defaultBlockState(), 3);
                relight(level, pos);
                GlowingGlass.LOGGER.info("[GlowingGlass] setBlock at {} -> lightBlocks={}, lightPanes={}",
                        pos, de.pasuki.glowing_glass.config.ConfigCache.lightBlocks,
                        de.pasuki.glowing_glass.config.ConfigCache.lightPanes);

                return true;
            }
            return true;
        }
        // Gefärbter Glas-Block
        if (block instanceof StainedGlassBlock sgb) {
            DyeColor color = sgb.getColor();
            BlockState newState = ModBlocks.COLORED_GLOWING_GLASS.get(color).get().defaultBlockState();
            level.setBlock(pos, newState, 3);
            relight(level, pos);
            // Klarer Glas-Block
            if (block == Blocks.GLASS) {
                level.setBlock(pos, ModBlocks.GLOWING_GLASS.get().defaultBlockState(), 3);
                relight(level, pos);
                GlowingGlass.LOGGER.info("[GlowingGlass] setBlock at {} -> lightBlocks={}, lightPanes={}",
                        pos, de.pasuki.glowing_glass.config.ConfigCache.lightBlocks,
                        de.pasuki.glowing_glass.config.ConfigCache.lightPanes);

                return true;
            }
            return true;
        }
        // Gefärbte Pane
        if (block instanceof StainedGlassPaneBlock spb) {
            DyeColor color = spb.getColor();
            BlockState newState = ModBlocks.COLORED_GLOWING_GLASS_PANES.get(color).get().defaultBlockState();
            level.setBlock(pos, copyPaneConnections(state, newState), 3);
            relight(level, pos);
            // Klarer Glas-Block
            if (block == Blocks.GLASS) {
                level.setBlock(pos, ModBlocks.GLOWING_GLASS.get().defaultBlockState(), 3);
                relight(level, pos);
                GlowingGlass.LOGGER.info("[GlowingGlass] setBlock at {} -> lightBlocks={}, lightPanes={}",
                        pos, de.pasuki.glowing_glass.config.ConfigCache.lightBlocks,
                        de.pasuki.glowing_glass.config.ConfigCache.lightPanes);

                return true;
            }
            return true;
        }

        return false;
    }

    private static boolean tryGlowingToVanilla(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // Klar
        if (block == ModBlocks.GLOWING_GLASS.get()) {
            level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            relight(level, pos);
            return true;
        }
        if (block == ModBlocks.GLOWING_GLASS_PANE.get()) {
            BlockState newState = Blocks.GLASS_PANE.defaultBlockState();
            level.setBlock(pos, copyPaneConnections(state, newState), 3);
            relight(level, pos);
            return true;
        }

        // Gefärbt (Block)
        for (var e : ModBlocks.COLORED_GLOWING_GLASS.entrySet()) {
            if (block == e.getValue().get()) {
                level.setBlock(pos, vanillaStained(e.getKey()).defaultBlockState(), 3);
                relight(level, pos);
                return true;
            }
        }
        // Gefärbt (Pane)
        for (var e : ModBlocks.COLORED_GLOWING_GLASS_PANES.entrySet()) {
            if (block == e.getValue().get()) {
                BlockState newState = vanillaStainedPane(e.getKey()).defaultBlockState();
                level.setBlock(pos, copyPaneConnections(state, newState), 3);
                relight(level, pos);
                return true;
            }
        }
        return false;
    }

    /* ----- Utility: Klassifizierer ----- */

    private static boolean isGlowingBlock(Block b) {
        if (b == ModBlocks.GLOWING_GLASS.get() || b == ModBlocks.GLOWING_GLASS_PANE.get())
            return true;
        for (var v : ModBlocks.COLORED_GLOWING_GLASS.values())
            if (b == v.get()) return true;
        for (var v : ModBlocks.COLORED_GLOWING_GLASS_PANES.values())
            if (b == v.get()) return true;
        return false;
    }

    private static boolean isVanillaBlock(Block b) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(b);
        return key != null && "minecraft".equals(key.getNamespace());
    }

    /* ----- Vanilla Mappings ----- */

    private static Block vanillaStained(DyeColor c) {
        return switch (c) {
            case WHITE -> Blocks.WHITE_STAINED_GLASS;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS;
            case GRAY -> Blocks.GRAY_STAINED_GLASS;
            case BLACK -> Blocks.BLACK_STAINED_GLASS;
            case BROWN -> Blocks.BROWN_STAINED_GLASS;
            case RED -> Blocks.RED_STAINED_GLASS;
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS;
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS;
            case LIME -> Blocks.LIME_STAINED_GLASS;
            case GREEN -> Blocks.GREEN_STAINED_GLASS;
            case CYAN -> Blocks.CYAN_STAINED_GLASS;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS;
            case BLUE -> Blocks.BLUE_STAINED_GLASS;
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS;
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS;
            case PINK -> Blocks.PINK_STAINED_GLASS;
        };
    }

    private static Block vanillaStainedPane(DyeColor c) {
        return switch (c) {
            case WHITE -> Blocks.WHITE_STAINED_GLASS_PANE;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS_PANE;
            case GRAY -> Blocks.GRAY_STAINED_GLASS_PANE;
            case BLACK -> Blocks.BLACK_STAINED_GLASS_PANE;
            case BROWN -> Blocks.BROWN_STAINED_GLASS_PANE;
            case RED -> Blocks.RED_STAINED_GLASS_PANE;
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS_PANE;
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS_PANE;
            case LIME -> Blocks.LIME_STAINED_GLASS_PANE;
            case GREEN -> Blocks.GREEN_STAINED_GLASS_PANE;
            case CYAN -> Blocks.CYAN_STAINED_GLASS_PANE;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS_PANE;
            case BLUE -> Blocks.BLUE_STAINED_GLASS_PANE;
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS_PANE;
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS_PANE;
            case PINK -> Blocks.PINK_STAINED_GLASS_PANE;
        };
    }

    /* ----- Pane-State kopieren ----- */

    private static BlockState copyPaneConnections(BlockState from, BlockState to) {
        for (BooleanProperty dir : new BooleanProperty[] {
                IronBarsBlock.NORTH, IronBarsBlock.EAST, IronBarsBlock.SOUTH, IronBarsBlock.WEST }) {
            if (from.hasProperty(dir) && to.hasProperty(dir)) {
                to = to.setValue(dir, from.getValue(dir));
            }
        }
        if (from.hasProperty(BlockStateProperties.WATERLOGGED) && to.hasProperty(BlockStateProperties.WATERLOGGED)) {
            to = to.setValue(BlockStateProperties.WATERLOGGED, from.getValue(BlockStateProperties.WATERLOGGED));
        }
        return to;
    }

    /* ----- Feedback: Sound & Partikel ----- */

    private static void feedback(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.15f);

        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            var dust = new net.minecraft.core.particles.DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.55f), 1.0f);
            server.sendParticles(dust, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    12, 0.35, 0.25, 0.35, 0.0);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    6, 0.25, 0.20, 0.25, 0.0);
        }
    }

    /* ----- Relight sofort anstoßen ----- */
    private static void relight(Level level, BlockPos pos) {
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }

    private GlowToggleHandler() {}
}
