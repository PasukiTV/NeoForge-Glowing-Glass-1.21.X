package de.pasuki.glowing_glass.block;

import de.pasuki.glowing_glass.GlowingGlass;
import de.pasuki.glowing_glass.item.ModItems;
import de.pasuki.glowing_glass.block.custom.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GlowingGlass.MOD_ID);

    public static final Map<DyeColor, DeferredBlock<Block>> COLORED_GLOWING_GLASS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, DeferredBlock<Block>> COLORED_GLOWING_GLASS_PANES = new EnumMap<>(DyeColor.class);

    private static boolean always(BlockState s, BlockGetter g, BlockPos p, EntityType<?> t) { return true; }
    private static boolean never (BlockState s, BlockGetter g, BlockPos p, EntityType<?> t) { return false; }

    // Clear block
    public static final DeferredBlock<Block> GLOWING_GLASS = registerBlock(
            "glowing_glass",
            () -> new ConfigLightGlassBlock(baseProps().isValidSpawn(ModBlocks::never))
    );

    // Clear pane
    public static final DeferredBlock<Block> GLOWING_GLASS_PANE = registerBlock(
            "glowing_glass_pane",
            () -> new ConfigLightGlassPaneBlock(baseProps())
    );

    // Colored variants (blocks + panes)
    static {
        for (DyeColor color : DyeColor.values()) {
            final String bName = color.getName() + "_glowing_glass";
            final String pName = color.getName() + "_glowing_glass_pane";

            DeferredBlock<Block> block = registerBlock(
                    bName,
                    () -> new ConfigLightStainedGlassBlock(color,
                            baseProps().mapColor(color.getMapColor()).isValidSpawn(ModBlocks::never))
            );
            COLORED_GLOWING_GLASS.put(color, block);

            DeferredBlock<Block> pane = registerBlock(
                    pName,
                    () -> new ConfigLightStainedGlassPaneBlock(color,
                            baseProps().mapColor(color.getMapColor()))
            );
            COLORED_GLOWING_GLASS_PANES.put(color, pane);
        }
    }

    private static BlockBehaviour.Properties baseProps() {
        return BlockBehaviour.Properties.of()
                .instrument(NoteBlockInstrument.HAT)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isRedstoneConductor((s,l,p) -> false)
                .isSuffocating((s,l,p) -> false)
                .isViewBlocking((s,l,p) -> false);
        // WICHTIG: KEIN .lightLevel(...) HIER!
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> reg = BLOCKS.register(name, block);
        registerBlockItem(name, reg);
        return reg;
    }
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) { BLOCKS.register(bus); }
}
