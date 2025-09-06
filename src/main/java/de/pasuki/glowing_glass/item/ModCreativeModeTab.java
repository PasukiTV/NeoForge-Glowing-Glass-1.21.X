package de.pasuki.glowing_glass.item;

import de.pasuki.glowing_glass.GlowingGlass;
import de.pasuki.glowing_glass.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GlowingGlass.MOD_ID);

    public static final Supplier<CreativeModeTab> GLOWING_GLASS = CREATE_MODE_TAB.register("glowing_glass_tab",
            ()-> CreativeModeTab.builder()
                    .icon(()-> new ItemStack(ModBlocks.GLOWING_GLASS.get()))
                    .title(Component.translatable("creativetab.glowing_glass"))
                    .displayItems((itemDisplayParameters, output) ->{
                        output.accept(ModBlocks.GLOWING_GLASS.get());

                        // Alle farbigen Varianten hinzufügen
                        for (var entry : ModBlocks.COLORED_GLOWING_GLASS.values()) {
                            output.accept(entry.get());
                        }

                        output.accept(ModBlocks.GLOWING_GLASS_PANE.get());

                        for (var b : ModBlocks.COLORED_GLOWING_GLASS_PANES.values()) {
                            output.accept(b.get());
                        }

                    })
                    .build());


    public static void register(IEventBus eventBus){
        CREATE_MODE_TAB.register(eventBus);
    }
}