package de.pasuki.glowing_glass.item;

import de.pasuki.glowing_glass.GlowingGlass;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GlowingGlass.MOD_ID);

    //public static final DeferredItem<Item> WHITE_DYE_DUST = ITEMS.register("white_dye_dust",
    //        () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
